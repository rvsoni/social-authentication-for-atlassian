var configuration = angular.module("openid.configuration", [])
    .constant('contextPath', contextPath)
    .config(['$interpolateProvider', function($interpolateProvider) {
        $interpolateProvider.startSymbol("[[");
        $interpolateProvider.endSymbol("]]");
    }]);

var ConfigurationCtrl = ['$scope', '$http', 'contextPath', function($scope, $http, contextPath) {
    $http.get(contextPath + "/rest/jira-openid-authentication/1.0/openIdProviders").success(function(data) {
        $scope.providers = data;
        $scope.loaded = true;
        $scope.error = false;
    }).error(function(data) {
        $scope.error = true;
    });
}];