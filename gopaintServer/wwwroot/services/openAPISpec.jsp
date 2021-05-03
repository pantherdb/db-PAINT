<%@page import="java.net.URLDecoder" contentType="text/html" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html;" charset="utf-8">
        <title>PAINT API</title>
        <link rel="stylesheet" type="text/css" href="/js/swagger/swagger-ui-3.18.3/css/swagger-ui.css">
        <link rel="icon" type="image/png" href="/js/swagger/swagger-ui-3.18.3/favicon-32x32.png" sizes="32x32" />
        <link rel="icon" type="image/png" href="/js/swagger/swagger-ui-3.18.3/favicon-16x16.png" sizes="16x16" />
    </head>
    <body>
        <div id="swagger-ui"></div>

        <script src="/js/swagger/swagger-ui-3.18.3/swagger-ui-bundle.js"></script>
        <script src="/js/swagger/swagger-ui-3.18.3/swagger-ui-standalone-preset.js"></script>

        <script>
        window.onload = function() {
            const ui = window.ui = new SwaggerUIBundle({
                url: "/services/api.jsp",
                validatorUrl: "" || null,
                dom_id: "#swagger-ui",
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset  //SwaggerUIStandalonePreset.slice(1) // No Topbar
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                displayOperationId: false,
                displayRequestDuration: false,
                docExpansion: "list"
            })
        }    
        </script>
    </body>
</html>
