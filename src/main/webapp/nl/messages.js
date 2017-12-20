'use strict';

var MESSAGES = {
    'error:no-results':             'Zoeken met uw gegevens in het BIG register leverde geen resultaten op. Neem contact op met irma \'at\' privacybydesign.foundation als u wel in het BIG register staat.',
    'error:multiple-results':       'Er zijn met uw gegevens meerdere resultaten gevonden in het BIG-register; daarom kunnen er geen eenduidige attributen uitgegeven worden.',
    'error:invalid-jwt':            'Kan de JWT niet verifieren - loopt de klok misschien ongelijk?',
    'error:big-request-failed':     'Communicatie met het BIG register werkt niet.',
    'error:connection':             'Probleem met de netwerkverbinding.',
    'request-disclosure-request':   'Requesting disclosure request...',
    'request-idin-attributes':      'Requesting iDIN attributes...',
    'error-cannot-request-idin':    'Kan de iDIN gegevens niet ophalen',
    'error-cannot-connect-backend': 'Kan geen verbinding maken met de backend server',
    'request-big-credentials':      'Requesting BIG credentials...',
    'error-unknown':                'Onbekend probleem',
    'error-no-results-header':      'Geen registratie gevonden in het BIG register',
    'error-backend-fail':           'De credentials kunnen niet aan de backend server gevraagd worden',
    'issue-start':                  'Issuing credential...',
    'issue-success':                'Credential voor het BIG register vrijgegeven',
    'issue-error':                  'Kan het BIG credential niet vrijgeven',
};
