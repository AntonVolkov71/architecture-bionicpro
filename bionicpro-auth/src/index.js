import 'dotenv/config';
import express from 'express';
import cookieParser from 'cookie-parser';
import cors from 'cors';
import axios from 'axios';
import crypto from 'crypto';
import jwt from 'jsonwebtoken';

const app = express();

app.use(express.json());
app.use(cookieParser());

app.use(cors({
    origin: process.env.FRONTEND_URL,
    credentials: true,
}));

const {
    PORT = 3001,
    FRONTEND_URL,
    KEYCLOAK_BASE_URL,
    KEYCLOAK_REALM,
    KEYCLOAK_CLIENT_ID,
    KEYCLOAK_CLIENT_SECRET,
    COOKIE_NAME = 'sid',
    COOKIE_SECURE = 'false',
    SESSION_ROTATION = 'true',
    ACCESS_SKEW_SECONDS = '10',
} = process.env;


const realmBase = `${KEYCLOAK_BASE_URL}/realms/${KEYCLOAK_REALM}`;

const KEYCLOAK_PUBLIC_URL = process.env.KEYCLOAK_PUBLIC_URL || KEYCLOAK_BASE_URL;
const KEYCLOAK_INTERNAL_URL = process.env.KEYCLOAK_INTERNAL_URL || process.env.KEYCLOAK_BASE_URL;

const authorizeUrl = `${KEYCLOAK_PUBLIC_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/auth`;
const tokenUrl = `${KEYCLOAK_INTERNAL_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`;

function log(info = true, serviceName, ...args) {
    if (info) {
        console.log('[' + serviceName + ']', ...args);
    } else {
        console.error('[' + serviceName + ']', ...args);
    }
}

function logInfo(serviceName, ...args) {
    log(true, serviceName, args)
}

function logError(serviceName, ...args) {
    log(false, serviceName, args)
}

// хранилище сессий
const sessions = new Map();

// генерим случай sid
function genSid() {
    return crypto.randomBytes(32).toString('base64url');
}

// ставим куку в ответе
function setSidCookie(res, sid) {
    res.cookie(COOKIE_NAME, sid, {
        httpOnly: true,
        secure: COOKIE_SECURE === 'true',
        sameSite: 'lax',
        path: '/',
    });
}

function clearSidCookie(res) {
    res.clearCookie(COOKIE_NAME, {path: '/'});
}


// Декодируем JWT access token - инфо о юзере
function decodeAccess(accessToken) {
    const decoded = jwt.decode(accessToken);
    if (!decoded || typeof decoded !== 'object') throw new Error('Cannot decode access token');

    const userId = decoded.sub;
    const roles =
        (decoded.realm_access && Array.isArray(decoded.realm_access.roles) && decoded.realm_access.roles) || [];

    const accessExp = decoded.exp; // seconds since epoch
    return {userId, roles, accessExp};
}

// чекаем истек access или нет (поправка на ветер 1 секунда)
function isAccessExpired(accessExpSec) {
    const skew = Number(ACCESS_SKEW_SECONDS) || 0;
    const now = Math.floor(Date.now() / 1000);
    return now >= (accessExpSec - skew);
}

// замена authorization_code на access/refresh токены
async function exchangeCodeForTokens(code, redirectUri) {
    const form = new URLSearchParams();
    form.set('grant_type', 'authorization_code');
    form.set('client_id', KEYCLOAK_CLIENT_ID);
    form.set('client_secret', KEYCLOAK_CLIENT_SECRET);
    form.set('code', code);
    form.set('redirect_uri', redirectUri);

    const {data} = await axios.post(tokenUrl, form, {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    });

    return {
        accessToken: data.access_token,
        refreshToken: data.refresh_token,
    };
}

// обновляем access token через refresh token (если access истёк)
async function refreshTokens(refreshToken) {

    const form = new URLSearchParams();
    form.set('grant_type', 'refresh_token');
    form.set('client_id', KEYCLOAK_CLIENT_ID);
    form.set('client_secret', KEYCLOAK_CLIENT_SECRET);
    form.set('refresh_token', refreshToken);

    const {data} = await axios.post(tokenUrl, form, {
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    });

    return {
        accessToken: data.access_token,
        refreshToken: data.refresh_token ?? refreshToken,
    };
}

// ротация сессии сразу меняем запись хардкорим запись в сторадже
function rotateSession(oldSid) {
    const s = sessions.get(oldSid);
    if (!s) return null;

    const newSid = genSid();
    sessions.set(newSid, {...s, createdAt: Date.now()});
    sessions.delete(oldSid);
    return newSid;
}

// редирект на Keycloak - отдаем форму входа KeycloakА
app.get('/login', (req, res) => {
    const redirectUri = `${req.protocol}://${req.get('host')}/callback`;

    logInfo('/login', {authorizeUrl, KEYCLOAK_PUBLIC_URL, KEYCLOAK_INTERNAL_URL});

    const state = crypto.randomBytes(16).toString('hex');

    const params = new URLSearchParams({
        client_id: KEYCLOAK_CLIENT_ID,
        redirect_uri: redirectUri,
        response_type: 'code',
        scope: 'openid',
        state,
    });

    res.redirect(`${authorizeUrl}?${params.toString()}`);
});

//  Keycloak возвращает code -> меняем на токены -> создаём sid -> ставим cookie -> редирект на фронт
app.get('/callback', async (req, res) => {
    try {
        const code = req.query.code;

        if (!code) return res.status(400).send('Missing code');

        const redirectUri = `${req.protocol}://${req.get('host')}/callback`;


        const {accessToken, refreshToken} = await exchangeCodeForTokens(code, redirectUri);

        logInfo('/callback', {
            accessToken: accessToken.length,
            refreshToken: refreshToken.length,
        });
        const {userId, roles, accessExp} = decodeAccess(accessToken);

        const nowSec = Math.floor(Date.now() / 1000);
        logInfo('/callback', {
            accessExp,
            nowSec,
            ttlSec: accessExp - nowSec,
        });

        const sid = genSid();

        logInfo('/callback', {
            code,
            redirectUri,
            accessToken: accessToken.length,
            refreshToken: refreshToken.length,
            userId,
            roles,
            accessExp
        });

        sessions.set(sid, {
            accessToken,
            refreshToken,
            accessExp,
            userId,
            roles,
            createdAt: Date.now(),
        });

        // Клиент получает только sid cookie, токены остаются на сервере
        setSidCookie(res, sid);

        // Возвращаем пользователя обратно в UI
        res.redirect(FRONTEND_URL);
    } catch (e) {
        console.error('callback error', e?.response?.data || e);
        clearSidCookie(res);
        res.status(500).send('Auth callback failed');
    }
});

// валидация sid от сервисов
// тута же: refresh access token + rotation sid.
app.post('/sessions/validate', async (req, res) => {
    try {
        // sid можно прислать в теле или взять из куки
        const sid = req.body?.sid || req.cookies?.[COOKIE_NAME];

        logInfo('/sessions/validate', {
            sid,
            sessionKeys: Array.from(sessions.keys()),
            size: sessions.size,
        })
        if (!sid) return res.status(401).json({valid: false});

        const s = sessions.get(sid);
        if (!s) return res.status(401).json({valid: false});

        let {accessToken, refreshToken, accessExp} = s;

        const nowSec = Math.floor(Date.now() / 1000);
        logInfo('/sessions/validate', {
            sid,
            nowSec,
            accessExp,
            ttlSec: accessExp - nowSec,
            expired: isAccessExpired(accessExp),
        });

        // если access истёк — обновляем через refresh
        if (isAccessExpired(accessExp)) {
            logInfo('/sessions/validate', {action: 'refreshTokens', sid});
            const refreshed = await refreshTokens(refreshToken);
            accessToken = refreshed.accessToken;
            refreshToken = refreshed.refreshToken;

            const decoded = decodeAccess(accessToken);
            accessExp = decoded.accessExp;

            sessions.set(sid, {
                ...s,
                accessToken,
                refreshToken,
                accessExp,
                userId: decoded.userId,
                roles: decoded.roles,
            });
        }

        // rotation
        let newSid = null;
        if (SESSION_ROTATION === 'true') {
            newSid = rotateSession(sid);

            if (newSid) {
                // обновляем кукис
                setSidCookie(res, newSid);
            }
        }

        const currentSid = newSid ?? sid;
        const current = sessions.get(currentSid) || sessions.get(sid);


        // не забываем про новый сид сессии
        return res.json({
            valid: true,
            userId: current.userId,
            roles: current.roles,
            newSid,
        });
    } catch (e) {
        console.error('validate error', e?.response?.data || e);
        return res.status(500).json({valid: false});
    }
});

// health
app.get('/health', (req, res) => res.json({ok: true}));

app.listen(PORT, () => {
    console.log(`bionicpro-auth listening on http://localhost:${PORT}`);
});
