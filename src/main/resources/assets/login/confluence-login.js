define('openid/marionette', ['backbone'], function (Backbone) {
    return Marionette.noConflict();
});

define('openid/providersModel', ['backbone', 'ajs'], function (Backbone, AJS) {
    return Backbone.Collection.extend({
        url: AJS.contextPath() + '/rest/jira-openid-authentication/1.0/openIdProviders/login'
    });
});

define('openid/providerView', ['openid/marionette', 'underscore', 'ajs'], function (Marionette, _, AJS) {
    return Marionette.ItemView.extend({
        tagName: 'span',
        className: 'provider',
        template: function (data) {
            return _.template('<a id="openid-<%= id %>" data-id="<%= id %>" class="openid aui-button" href="<%= authenticationUrl %>"><%= name %></a>')(data);
        },
        serializeData: function() {
            return _.extend(this.model.toJSON(), {
                authenticationUrl: this.getAuthenticationUrl(this.model.get('id'))
            })
        },
        getParameterByName: function (name, href) {
            name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
            var regexS = "[\\?&]" + name + "=([^&#]*)";
            var regex = new RegExp(regexS);
            var results = regex.exec(href);
            if (results == null)
                return "";
            else
                return decodeURIComponent(results[1].replace(/\+/g, " "));
        },
        getAuthenticationUrl: function(providerId) {
            var authenticationUrl = AJS.contextPath() + '/plugins/servlet/openid-login?pid=' + providerId;
            var returnUrl = this.getParameterByName("os_destination", window.location.href);
            if (returnUrl) {
                authenticationUrl += "&returnUrl=" + encodeURIComponent(returnUrl);
            }
            return authenticationUrl;
        }
    });
});

define('openid/emptyView', ['openid/marionette'], function (Marionette) {
    return Marionette.ItemView.extend({
        tagName: 'span',
        className: 'empty',
        template: function () {
            return 'All OpenID providers were disabled'
        }
    });
});

define('openid/loginView', ['openid/marionette', 'openid/providersModel', 'openid/providerView', 'openid/emptyView', 'underscore'],
    function (Marionette, ProvidersModel, ProviderView, EmptyView, _) {
        return Marionette.CompositeView.extend({
            el: '#openid',
            childView: ProviderView,
            childViewContainer: '.providers',
            emptyView: EmptyView,
            template: function (data) {
                return _.template('<div class="divider"><span>or</span></div><div class="providers"></div>')(data);
            },
            onRender: function () {
                console.log("Rendering loginView");
            }
        });
    });

require(['ajs', 'jquery', 'openid/marionette', 'openid/loginView', 'openid/providersModel'],
    function (AJS, $, Marionette, LoginView, ProvidersModel) {
        console.log('OpenID booting up...');
        $('.login-section').append('<div id="openid"></div>');
        var providers = new ProvidersModel();
        var login = new LoginView({collection: providers});
        providers.fetch({
            success: login.render
        });

        return;

        var $login = $("#login-form"); // check JIRA form first


        if ($login.length && (!$login.attr('action') || $login.attr('action').indexOf('WebSudo') == -1)) {
            var buttons = [];

            buttons.push('<button id="openid-button" class="aui-button aui-dropdown2-trigger" href="#openid-providers" aria-owns="openid-providers"'
                + ' aria-haspopup="true" aria-controls="openid-providers">OpenID Login</button>');
            buttons.push('<div id="openid-providers" class="aui-dropdown2 aui-style-default" aria-hidden="true" data-dropdown2-alignment="left">');
            buttons.push('<ul class="aui-list-truncate">');
            buttons.push('<li><a href="#" class="loading"><span class="aui-icon aui-icon-wait"></span> Loading, please wait</a></li>');
            buttons.push('</ul>');

            $(buttons.join("")).insertAfter($(".buttons-container.form-footer .buttons input:first"));

            $.ajax(contextPath + "/rest/jira-openid-authentication/1.0/openIdProviders/login").done(function (data) {
                var $providers = $("#openid-providers ul");

                $providers.find("li a.loading").remove();

                if ($.isArray(data) && data.length > 0) {
                    var openIds = [];

                    $(data).each(function (idx, obj) {
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
                    $providers.find("li a").click(function () {
                        $("#login-form").removeDirtyWarning();
                    });
                } else {
                    $providers.append("<li><a href='#'>All OpenID providers were disabled</a></li>");
                }
            });
        }

    });
