angular.module("openid.configuration", ['ngRoute'])
    .constant('contextPath', contextPath)
    .constant('baseUrl', angular.element('meta[name="ajs-base-url"]').attr('content'))
    .constant('restPath', contextPath + "/rest/jira-openid-authentication/1.0")
    .factory('externalUserManagement', function() {
        return angular.element('div[ng-app="openid.configuration"]').data('external-user-management');
    })
    .factory('publicMode', function() {
        return angular.element('div[ng-app="openid.configuration"]').data('public-mode');
    })
    .factory('creatingUsers', function() {
        return angular.element('div[ng-app="openid.configuration"]').data('creating-users');
    })
    .config(['$routeProvider', 'restPath', function ($routeProvider, restPath) {
        $routeProvider
            .when('/', {
                controller: 'ProvidersCtrl',
                templateUrl: restPath + '/templates/OpenId.Templates.Configuration.providers'
            })
            .when('/edit/:providerId', {
                controller: 'EditProviderCtrl',
                templateUrl: restPath + '/templates/OpenId.Templates.Configuration.editProvider'
            })
            .when('/create', {
                controller: 'CreateProviderCtrl',
                templateUrl: restPath + '/templates/OpenId.Templates.Configuration.createProvider'
            })
            .otherwise({ redirectTo: '/' });
    }])
    .controller('ProvidersCtrl', ['$scope', '$http', 'restPath', 'externalUserManagement',
            'publicMode', 'creatingUsers', function ($scope, $http, restPath, externalUserManagement, publicMode, creatingUsers) {
        $scope.isPublicMode = publicMode;
        $scope.isExternalUserManagement = externalUserManagement;
        $scope.isCreatingUsers = creatingUsers;

        var setProviders = function (data) {
            $scope.providers = data;
            $scope.loaded = true;
            $scope.error = false;
        };

        $scope.creatingUsers = function(create) {
            $http.put(restPath + "/settings", {creatingUsers: create}).success(
                function(response) {
                    $scope.isCreatingUsers = response.creatingUsers;
                }
            );
        };

        $scope.moveProviderUp = function (providerId) {
            $http.post(restPath + "/openIdProviders/moveUp/" + providerId).success(setProviders);
        };

        $scope.moveProviderDown = function (providerId) {
            $http.post(restPath + "/openIdProviders/moveDown/" + providerId).success(setProviders);
        };

        $http.get(restPath + "/openIdProviders").success(setProviders).error(function (data) {
            $scope.error = true;
        });
    }])
    .controller('CreateProviderCtrl', ['$scope', 'baseUrl', function ($scope, baseUrl) {
        $scope.providerType = "oauth2";
        $scope.callbackId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
        $scope.callbackUrl = baseUrl + "/openid/oauth2-callback/" + $scope.callbackId;
    }])
    .controller('EditProviderCtrl', [function () {

    }]);

//(function($) {
//    $(function () {
//        var showOrHide = function () {
//            if ($('#openid1').attr('checked')) {
//                $('.oauth2').hide();
//                $('.openid1').show();
//            } else {
//                $('.oauth2').show();
//                $('.openid1').hide();
//            }
//        };
//
//        $('input[name=providerType]').click(showOrHide);
//        showOrHide();
//
//        $('.preset').click(function() {
//            var $this = $(this);
//
//            $('input[name=name]').val($this.data("name"));
//            $('input[name=endpointUrl]').val($this.data("endpointurl"));
//
//            if ($this.data("hint")) {
//                $('.hint > .hint-text').html($this.data("hint"));
//                $('.hint').removeClass('hidden');
//            } else {
//                $('.hint').addClass('hidden');
//            }
//
//            if ($this.data("providertype") == "oauth2") {
//                $('#oauth2').attr('checked', true);
//            } else {
//                $('#openid1').attr('checked', true);
//                $('input[name=extensionNamespace]').val($this.data("extensionnamespace"));
//            }
//            showOrHide();
//        });
//    });
//}(AJS.$));