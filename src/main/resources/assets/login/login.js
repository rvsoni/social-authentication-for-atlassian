define('openid/product', ['jquery'], function ($) {
    var appName = $('meta[name=application-name]').data('name');
    if (appName) {
        return {name: appName};
    } else {
        return {name: 'confluence'};
    }
});

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
        serializeData: function () {
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
        getAuthenticationUrl: function (providerId) {
            var authenticationUrl = AJS.contextPath() + '/openid/login/' + providerId;
            var returnUrl = this.getParameterByName("os_destination", window.location.href);
            if (returnUrl) {
                authenticationUrl += "?returnUrl=" + encodeURIComponent(returnUrl);
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

require(['ajs', 'jquery', 'openid/marionette', 'openid/loginView', 'openid/providersModel', 'openid/product', 'underscore'],
    function (AJS, $, Marionette, LoginView, ProvidersModel, Product, _) {
        $(document).ready(function () {
            console.log('OpenID booting up...');

            var isJIRA = Product.name === 'jira';
            if (isJIRA && $('#dashboard').length) {
                console.log('Dashboard detected, waiting for elements...');

                var f = function() {
                    if (!modifyLoginForm()) {
                        setTimeout(f, 100);
                    }
                };

                setTimeout(f, 100);
            } else {
                modifyLoginForm();
            }

            function modifyLoginForm() {
                var loginFormSelector = isJIRA ? "#login-form, #loginform" : "form.login-form-container";
                var $loginForm = $(loginFormSelector);
                if (!$loginForm.length || !$loginForm.attr('action') || $loginForm.attr('action').indexOf('WebSudo') != -1) {
                    console.log('Login form has no action or that is WebSudo');
                    return false;
                }

                if (_.isFunction($loginForm.removeDirtyWarning)) {
                    console.log('Disabling dirty warning');
                    $loginForm.removeDirtyWarning();
                }

                var $attachLocation = isJIRA ? $loginForm : $('.login-section');
                $attachLocation.append('<div id="openid-login"></div>');
                var providers = new ProvidersModel();
                var login = new LoginView({collection: providers});
                providers.fetch({
                    success: login.render
                });
                return true;
            }
        });
    });
