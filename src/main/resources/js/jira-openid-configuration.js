var configuration = angular.module("openid.configuration", [])
    .constant('contextPath', contextPath)
    .config(['$interpolateProvider', function($interpolateProvider) {
        $interpolateProvider.startSymbol("[[");
        $interpolateProvider.endSymbol("]]");
    }]);

var ConfigurationCtrl = ['$scope', '$http', 'contextPath', function($scope, $http, contextPath) {
    $scope.sortingLog = [];

    var tmpList = [];

    for (var i = 1; i <= 6; i++){
        tmpList.push({
            text: 'Item ' + i,
            value: i
        });
    }

    $scope.list = tmpList;


    $scope.sortableOptions = {
        stop: function(e, ui) {
            var logEntry = {
                ID: $scope.sortingLog.length + 1,
                Text: 'Moved element: ' + ui.item.scope().item.text
            };
            $scope.sortingLog.push(logEntry);
        }
    };

    $http.get(contextPath + "/rest/jira-openid-authentication/1.0/openIdProviders").success(function(data) {
        $scope.providers = data;
        $scope.loaded = true;
        $scope.error = false;
    }).error(function(data) {
        $scope.error = true;
    });
}];