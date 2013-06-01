AJS.$(function() {
    if (AJS.$("#login-form").length) {
        var openIds = [];

        openIds.push(
            AJS.$('<a id="open-id-google" class="aui-button open-id" href="' + contextPath + '/plugins/servlet/open-id-authentication?op=Google">Log In with Google</a>'));

        openIds.push(
            AJS.$('<a id="open-id-yahoo" class="aui-button open-id" href="' + contextPath + '/plugins/servlet/open-id-authentication?op=Yahoo">Log In with Yahoo</a>'));

        AJS.$(openIds).insertAfter(AJS.$(".buttons-container.form-footer .buttons input:first"));
        AJS.$(".aui-button.open-id").click(function() {
            AJS.$(this).removeDirtyWarning();
        });
    }
});
