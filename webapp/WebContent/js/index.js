var app = angular.module("main", []);
app.controller('mainCtrl', ['$scope','$http', function($scope,$http) {

    /************************************
      important:
      in google chrome new tab/window does not create new session,
      therefore for checking the website with two users we need
      to open the website in private window. 
      (and all private windows have same session)
    *************************************/
    
    angular.element(document).ready(function () {
        /*
          we send request to servlet, to check if session is opened.
          if yes we display the main page - the channels.
          else we display to log in/sign up page
        */
        $http.get("http://localhost:8080/webapp/checkSession") 
        .success(function(response) {
            // if no session created - user did not log in
            if(response=="no"){
                // display page1 - login/signup
                $("#page1").css("display", "block");
            }
            // if there is session the servlet return the username of current user
            else {     
                $("#page1").css("display", "none");
                // request for user details
                $http.get("http://localhost:8080/webapp/getUserDetails") 
                .success(function(response) {
                    // update page2 with user information
                    $("#nickname").text(response.nickname);
                    $("#profile img").attr("src",response.photo);
                    $(".page2").css("display", "block");
                    // we move the scrollbar to the bottom of the page/chat
                    $('#chat').scrollTop($('#chat')[0].scrollHeight - $('#chat')[0].clientHeight);
                    // connect to the websocket
                    connect(response.userName);
                });            
            }
        });    
    });
}]);

