angular.module("openid.configuration", ['ngRoute'])
    .constant('contextPath', contextPath)
    .constant('baseUrl', angular.element('meta[name="ajs-base-url"]').attr('content'))
    .constant('restPath', contextPath + "/rest/jira-openid-authentication/1.0")
    .factory('externalUserManagement', function() {
        return WRM.data.claim('openid.externalUserManagement');
    })
    .factory('publicMode', function() {
        return WRM.data.claim('openid.publicMode');
    })
    .factory('creatingUsers', function() {
        return WRM.data.claim('openid.creatingUsers');
    })
    .factory('errorHandler', ['$window', function($window) {
        return {
            handleError: function(data, status) {
                if (status == 401) {
                    $window.location.replace(contextPath + '/login.jsp?permissionViolation=true&os_destination=' + encodeURIComponent($window.location.href));
                }
            }
        }
    }])
    .factory('providers', ['$q', '$http', '$log', 'restPath', function($q, $http, $log, restPath) {
        var providers = [];
        return {
            getProviders: function() {
                var deferred = $q.defer();
                if (providers == undefined || !providers.length) {
                    $http.get(restPath + "/providers").success(function(response) {
                        providers = response;
                        deferred.resolve(response);
                    }).error(function(msg, code) {
                        deferred.reject(msg);
                        $log.error(msg, code);
                    });
                } else {
                    deferred.resolve(providers);
                }
                return deferred.promise;
            },
            getProviderById: function(providerId) { return _.find(providers, function(p) { return p.id == providerId; } )}
        }
    }])
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
            .when('/delete/:providerId', {
                controller: 'DeleteProviderCtrl',
                templateUrl: restPath + '/templates/OpenId.Templates.Configuration.deleteProvider'
            })
            .when('/create', {
                controller: 'CreateProviderCtrl',
                templateUrl: restPath + '/templates/OpenId.Templates.Configuration.createProvider'
            })
            .otherwise({ redirectTo: '/' });
    }])
    .controller('ProvidersCtrl', ['$scope', '$http', 'restPath', 'externalUserManagement',
            'publicMode', 'creatingUsers', 'providers', function ($scope, $http, restPath, externalUserManagement, publicMode, creatingUsers, providers) {
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
            $http.post(restPath + "/providers/moveUp/" + providerId).success(setProviders);
        };

        $scope.moveProviderDown = function (providerId) {
            $http.post(restPath + "/providers/moveDown/" + providerId).success(setProviders);
        };

        $scope.enableProvider = function(providerId, enabled) {
            $http.post(restPath + "/providers/" + providerId + "/state", { enabled: enabled }).success(setProviders);
        };

        providers.getProviders().then(setProviders, function (data) {
            $scope.error = true;
        });
    }])
    .controller('CreateProviderCtrl', ['$scope', '$location', '$http', 'restPath', 'baseUrl', 'errorHandler',
        function ($scope, $location, $http, restPath, baseUrl, errorHandler) {
        $scope.provider = { providerType: "oauth2", extensionNamespace: 'ext1' };
        $scope.provider.callbackId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
        $scope.provider.callbackUrl = baseUrl + "/openid/oauth2-callback/" + $scope.callbackId;

        $scope.createProvider = function() {
            $scope.errors = {};
            $scope.errorMessages = [];

            $http.post(restPath + '/providers', $scope.provider).success(function(response) {
                if (response.errorMessages == undefined && response.errors == undefined) {
                    $location.path('/');
                } else {
                    $scope.errors = response.errors;
                    $scope.errorMessages = response.errorMessages;
                }
            }).error(errorHandler.handleError);
        }
    }])
    .controller('EditProviderCtrl', ['$routeParams', '$scope', '$location', '$http', 'providers', 'restPath', 'errorHandler',
        function($routeParams, $scope, $location, $http, providers, restPath, errorHandler) {
        var providerId = $routeParams.providerId;

        providers.getProviders().then(function() {
            $scope.provider = providers.getProviderById(providerId);

            if ($scope.provider == undefined) {
                $location.path('/');
            }
        });

        $scope.updateProvider = function() {
            $scope.errors = {};
            $scope.errorMessages = [];

            $http.put(restPath + '/providers/' + providerId, $scope.provider).success(function(response) {
                if (response.errorMessages == undefined && response.errors == undefined) {
                    $location.path('/');
                } else {
                    $scope.errors = response.errors;
                    $scope.errorMessages = response.errorMessages;
                }
            }).error(errorHandler.handleError);
        };
    }])
    .controller('DeleteProviderCtrl', ['$routeParams', '$scope', '$location', '$http', 'providers', 'restPath',
        function($routeParams, $scope, $location, $http, providers, restPath) {
        var providerId = $routeParams.providerId;

        providers.getProviders().then(function() {
            $scope.provider = providers.getProviderById(providerId);

            if ($scope.provider == undefined) {
                $location.path('/');
            }
        });

        $scope.deleteProvider = function() {
            $http['delete'](restPath + '/providers/' + providerId).success(function() {
                $location.path('/');
            });
        };
    }]);