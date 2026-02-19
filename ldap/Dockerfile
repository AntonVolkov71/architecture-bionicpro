FROM osixia/openldap:1.5.0

COPY 50-users.ldif /container/service/slapd/assets/config/bootstrap/ldif/custom/50-users.ldif

ENV LDAP_TLS=false