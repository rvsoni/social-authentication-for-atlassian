AJS.$(function() {
    if (AJS.$("#login-form").length) {
        var buttons = [];

        buttons.push('<button id="openid-button" class="aui-button aui-dropdown2-trigger" href="#openid-providers" aria-owns="openid-providers"'
            +' aria-haspopup="true" aria-controls="openid-providers">OpenID Login</button>');
        buttons.push('<div id="openid-providers" class="aui-dropdown2 aui-style-default" aria-hidden="true" data-dropdown2-alignment="left">');
        buttons.push('<ul class="aui-list-truncate">');
        buttons.push('<li><a href="#" class="loading"><span class="aui-icon aui-icon-wait"></span> Loading, please wait</a></li>');
        buttons.push('</ul>');

        AJS.$(buttons.join("")).insertAfter(AJS.$(".buttons-container.form-footer .buttons input:first"));

        AJS.$.ajax(contextPath + "/rest/jira-openid-authentication/1.0/openIdProviders/login").done(function(data) {
			var $providers = AJS.$("#openid-providers ul");

			$providers.find("li a.loading").remove();

            if (AJS.$.isArray(data) && data.length > 0) {
                var openIds = [];

                AJS.$(data).each(function(idx, obj) {
                    var authenticationUrl = contextPath + '/plugins/servlet/openid-login?pid=' + obj.id;
                    var returnUrl = getParameterByName("os_destination", window.location.href);
                    if (returnUrl) {
                        authenticationUrl += "&returnUrl=" + encodeURIComponent(returnUrl);
                    }

                    openIds.push(
                        '<li><a id="openid-' + obj.id + '" class="openid" href="'
                            + authenticationUrl + '">' + obj.name + '</a></li>');
                });
                $providers.append(openIds.join(""));
                $providers.find("li a").click(function() {
                    AJS.$("#login-form").removeDirtyWarning();
                });
            } else {
				$providers.append("<li><a href='#'>All OpenID providers were disabled</a></li>");
			}
        });
    }

    function getParameterByName(name, href)
    {
        name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
        var regexS = "[\\?&]"+name+"=([^&#]*)";
        var regex = new RegExp( regexS );
        var results = regex.exec( href );
        if( results == null )
            return "";
        else
            return decodeURIComponent(results[1].replace(/\+/g, " "));
    }
});
