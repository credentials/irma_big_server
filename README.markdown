# BIG register client app

This app provides credentials from the
[BIG register](https://www.bigregister.nl/zoek-zorgverlener/zoeken-eigen-systeem)

This app uses iDIN attributes (from your bank and verified there) to search for
your registration in the BIG register.

## OS X SSL problem

On OS X, you may encounter this error:

> javax.net.ssl.SSLHandshakeException: SSLHandshakeException invoking
> https://webservices.cibg.nl/Ribiz/OpenbaarV4.asmx:
> sun.security.validator.ValidatorException: PKIX path building failed:
> sun.security.provider.certpath.SunCertPathBuilderException: unable to find
> valid certification path to requested target

This can be resolved by manually importing the "Staat der Nederlanden Root CA -
G2" certificate, with something like:

    sudo keytool -keystore $(find $JAVA_HOME -name cacerts) storepass changeit -importcert -alias staatdernederlandeng2 -file StaatderNederlandenRootCA-G2.der

