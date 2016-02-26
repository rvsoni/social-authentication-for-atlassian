define('easy-sign-ups/product', ['servicedesk/jQuery'], function ($) {
    var appName = $('meta[name=application-name]').data('name');
    if (appName) {
        return {name: appName};
    } else {
        return {name: 'confluence'};
    }
});

define('easy-sign-ups/marionette', ['servicedesk/backbone'], function (Backbone) {
    return Marionette.noConflict();
});

define('easy-sign-ups/providersModel', ['servicedesk/backbone', 'ajs', 'servicedesk/util/context-path'], function (Backbone, AJS, contextPath) {
    return Backbone.Collection.extend({
        url: contextPath + '/rest/easy-sign-ups/1.0/openIdProviders/login'
    });
});

define('easy-sign-ups/providerView', ['easy-sign-ups/marionette', 'servicedesk/underscore', 'ajs'], function (Marionette, _, AJS) {
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
        getPortalId: function() {
            return window.location.pathname.match(/servicedesk\/customer\/portal\/([0-9]*)\/user/)[1];
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
            var authenticationUrl = AJS.contextPath() + '/easy-sign-ups/login/' + providerId + '?portalId=' + this.getPortalId();

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

define('easy-sign-ups/loginView', ['easy-sign-ups/marionette', 'easy-sign-ups/providersModel', 'easy-sign-ups/providerView', 'easy-sign-ups/emptyView', 'servicedesk/underscore'],
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

require(['ajs', 'servicedesk/jQuery', 'easy-sign-ups/marionette', 'easy-sign-ups/loginView', 'easy-sign-ups/providersModel', 'easy-sign-ups/product', 'servicedesk/underscore'],
    function (AJS, $, Marionette, LoginView, ProvidersModel, Product, _) {
        $(document).ready(function () {
            console.log('Easy sign-ups booting up...');

            var loginFormSelector = ".cv-login .cv-main-group .cv-col-secondary";
            var $loginForm = $(loginFormSelector);

            if (_.isFunction($loginForm.removeDirtyWarning)) {
                console.log('Disabling dirty warning');
                $loginForm.removeDirtyWarning();
            }

            var $attachLocation = $loginForm;
            $attachLocation.append('<div id="openid-login"></div>');
            var providers = new ProvidersModel();
            var login = new LoginView({collection: providers});
            providers.fetch({
                success: login.render
            });
            return true;
        });
    });
