'use strict';

var MESSAGES = {
    'error:no-results':             'Zoeken met uw gegevens in het BIG register leverde geen resultaten op. Neem contact op met irma \'at\' privacybydesign.foundation als u wel in het BIG register staat.',
    'error:multiple-results':       'Er zijn met uw gegevens meerdere resultaten gevonden in het BIG-register; daarom kunnen er geen eenduidige attributen uitgegeven worden.',
    'error:invalid-jwt':            'Kan de JWT niet verifieren - loopt de klok misschien ongelijk?',
    'error:big-request-failed':     'Communicatie met het BIG register werkt niet.',
    'error:connection':             'Probleem met de netwerkverbinding.',
    'request-disclosure-request':   'Attribuut-verificatie starten...',
    'request-idin-attributes':      'iDIN-attributen opvragen...',
    'error-cannot-request-idin':    'Kan de iDIN gegevens niet ophalen',
    'error-cannot-connect-backend': 'Kan geen verbinding maken met de backend server',
    'request-big-credentials':      'BIG-attributen laden...',
    'error-unknown':                'Onbekend probleem',
    'error-no-results-header':      'Geen registratie gevonden in het BIG register',
    'error-backend-fail':           'De attributen kunnen niet aan de backend server gevraagd worden',
    'issue-start':                  'BIG-attributen uitgeven...',
    'issue-success':                'BIG-attributen uitgegeven',
    'issue-error':                  'Kan de BIG-attributen niet uitgeven',
};
