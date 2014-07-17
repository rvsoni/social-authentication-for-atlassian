var configuration = angular.module("openid.configuration", [])
    .constant('contextPath', contextPath)
    .constant('restPath', contextPath + "/rest/jira-openid-authentication/1.0")
    .config(['$interpolateProvider', function($interpolateProvider) {
        $interpolateProvider.startSymbol("[[");
        $interpolateProvider.endSymbol("]]");
    }]);

var ConfigurationCtrl = ['$scope', '$http', 'restPath', function($scope, $http, restPath) {
    var setProviders = function(data) {
        $scope.providers = data;
        $scope.loaded = true;
        $scope.error = false;
    };

    $scope.moveProviderUp = function(providerId) {
        $http.post(restPath + "/openIdProviders/moveUp/" + providerId).success(setProviders);
    };

    $scope.moveProviderDown = function(providerId) {
        $http.post(restPath + "/openIdProviders/moveDown/" + providerId).success(setProviders);
    };

    $http.get(restPath + "/openIdProviders").success(setProviders).error(function(data) {
        $scope.error = true;
    });
}];

(function($) {
    $(function () {
        var showOrHide = function () {
            if ($('#openid1').attr('checked')) {
                $('.oauth2').hide();
                $('.openid1').show();
            } else {
                $('.oauth2').show();
                $('.openid1').hide();
            }
        };

        $('input[name=providerType]').click(showOrHide);
        showOrHide();

        $('.preset.google').click(function() {
            $('input[name=endpointUrl]').val('https://accounts.google.com/o/oauth2/auth');
            $('#oauth2').attr('checked', true);
            showOrHide();
        });

        $('.preset.yahoo').click(function() {
            $('input[name=endpointUrl]').val('http://open.login.yahooapis.com/openid20/www.yahoo.com/xrds');
            $('input[name=extensionNamespace]').val('ax');
            $('#openid1').attr('checked', true);
            showOrHide();
        });
    });
}(AJS.$));