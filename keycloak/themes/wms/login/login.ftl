<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WMS — Sign in</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css">
</head>
<body>
    <div class="wms-page">
        <div class="wms-card">

            <div class="wms-brand">
                <div class="wms-brand-mark">&#9881;&#65039;</div>
                <span class="wms-brand-text">WMS</span>
            </div>

            <h1 class="wms-title">Sign in</h1>
            <p class="wms-subtitle">Enter your credentials to continue.</p>

            <#if message?? && message.type = "error">
            <div class="wms-error">${message.summary}</div>
            </#if>

            <form action="${url.loginAction}" method="post">
                <input type="hidden" id="id-hidden-input" name="credentialId"
                    <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>

                <div class="wms-field">
                    <label class="wms-label" for="username">Username</label>
                    <input class="wms-input" id="username" name="username" type="text"
                        value="${login.username!''}"
                        autocomplete="username"
                        autofocus/>
                </div>

                <div class="wms-field">
                    <label class="wms-label" for="password">Password</label>
                    <input class="wms-input" id="password" name="password" type="password"
                        autocomplete="current-password"/>
                </div>

                <button class="wms-btn" type="submit">Sign in</button>
            </form>

        </div>
    </div>
</body>
</html>
