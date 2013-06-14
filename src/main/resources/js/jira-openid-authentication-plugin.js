AJS.$(function() {
    if (AJS.$("#login-form").length) {
        var openIds = [];

        openIds.push(
            '<a id="open-id-google" class="aui-button open-id" href="' + contextPath + '/plugins/servlet/openid-authentication?op=Google">Using Google</a>');

        openIds.push(
            '<a id="open-id-yahoo" class="aui-button open-id" href="' + contextPath + '/plugins/servlet/openid-authentication?op=Yahoo">Or Yahoo</a>');

        AJS.$(openIds.join("")).insertAfter(AJS.$(".buttons-container.form-footer .buttons input:first"));
        AJS.$(".aui-button.open-id").click(function() {
            AJS.$(this).removeDirtyWarning();
        });
    }
});
