define('easy-sign-ups/underscore', ['atlassian/libs/underscore-1.8.3'], function(factory) {
    return factory;
});

define('easy-sign-ups/backbone', ['jquery', 'easy-sign-ups/underscore', 'atlassian/libs/factories/backbone-1.3.3'], function($, _, factory) {
    return factory(_, $);
});

define('easy-sign-ups/marionette', ['easy-sign-ups/backbone', 'easy-sign-ups/underscore', 'atlassian/libs/factories/marionette-2.1.0'], function (Backbone, _, factory) {
    return factory(_, Backbone);
});

define('easy-sign-ups/providersModel', ['easy-sign-ups/backbone', 'wrm/context-path'], function (Backbone, contextPath) {
    return Backbone.Collection.extend({
        url: contextPath() + '/rest/easy-sign-ups/1.0/openIdProviders/login'
    });
});

define('easy-sign-ups/providerView', ['easy-sign-ups/marionette', 'easy-sign-ups/underscore', 'wrm/context-path'], function (Marionette, _, contextPath) {
    return Marionette.ItemView.extend({
        tagName: 'span',
        className: 'provider',
        template: function (data) {
            return _.template('<a id="openid-<%= id %>" data-id="<%= id %>" class="openid aui-button button" href="<%= authenticationUrl %>"><%= name %></a>')(data);
        },
        serializeData: function () {
            return _.extend(this.model.toJSON(), {
                authenticationUrl: this.getAuthenticationUrl(this.model.get('id'))
            })
        },
        getPortalId: function() {
            var match = window.location.pathname.match(/servicedesk\/customer\/portal\/([0-9]*)\/user/);
            return match ? match[1] : "";
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
        getAuthenticationUrl: function (providerId) {
            var authenticationUrl = contextPath() + '/easy-sign-ups/login/' + providerId + '?portalId=' + this.getPortalId();

            var returnUrl = this.getParameterByName("destination", window.location.href);
            if (returnUrl) {
                authenticationUrl += "&returnUrl=" + encodeURIComponent(returnUrl);
            }
            return authenticationUrl;
        }
    });
});

define('easy-sign-ups/emptyView', ['easy-sign-ups/marionette'], function (Marionette) {
    return Marionette.ItemView.extend({
        tagName: 'span',
        className: 'empty',
        template: function () {
            return 'All providers were disabled'
        }
    });
});

define('easy-sign-ups/loginView', ['easy-sign-ups/marionette', 'easy-sign-ups/providersModel', 'easy-sign-ups/providerView', 'easy-sign-ups/emptyView', 'easy-sign-ups/underscore'],
    function (Marionette, ProvidersModel, ProviderView, EmptyView, _) {
        return Marionette.CompositeView.extend({
            el: '#openid-login',
            childView: ProviderView,
            childViewContainer: '.providers',
            emptyView: EmptyView,
            template: function (data) {
                return _.template('<div class="divider"><span>or</span></div><div class="providers"></div>')(data);
            },
            onRender: function () {
                console.log("Rendering providers container...");
            }
        });
    });

require(['jquery', 'easy-sign-ups/marionette', 'easy-sign-ups/loginView', 'easy-sign-ups/providersModel', 'easy-sign-ups/underscore'],
    function ($, Marionette, LoginView, ProvidersModel, _) {
        $(document).ready(function () {
            console.log('Easy sign-ups booting up...');

            if ($('meta[name=loggedInUser]').length) {
                console.log('User is already logged in');
                return;
            }

            waitForLoginForm();

            $(window).on('popstate', waitForLoginForm);

            function waitForLoginForm() {
                var retries = 100;

                var f = function () {
                    if (!modifyLoginForm() && retries >= 0) {
                        retries -= 1;
                        setTimeout(f, 100);
                    }
                };

                setTimeout(f, 50);
            }

            function modifyLoginForm() {
                var loginFormSelector = ".cv-login .cv-main-group .cv-col-secondary";
                var $loginForm = $(loginFormSelector);

                if ($('#user-signup-form').length) {
                    console.log('Sign up page detected');
                    return true;
                }

                if (!$loginForm.length) {
                    console.log('Login form not found');
                    return false;
                }

                if (_.isFunction($loginForm.removeDirtyWarning)) {
                    console.log('Disabling dirty warning');
                    $loginForm.removeDirtyWarning();
                }

                $loginForm.append('<div id="openid-login"></div>');

                var providers = new ProvidersModel();
                var login = new LoginView({collection: providers});
                providers.fetch({
                    success: login.render
                });
                return true;
            }
        });
    });
