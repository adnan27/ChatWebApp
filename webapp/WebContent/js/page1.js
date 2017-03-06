
app.controller('loginCtrl', ['$scope','$http', function($scope,$http) {
    // function the called when user click "submit" in login form
    $scope.submit=function(){
        // if user did not enter username or password
        if(!$scope.username||!$scope.password)
            return;
        // we send user details to signin servlet, for checking if correct
        var url="signin/username/"+$scope.username+"/password/"+$scope.password;
        $http.get("http://localhost:8080/webapp/"+url) 
            .success(function(response) {  
                if(response=="sucess"){
                    var username=userLogin.value;
                    // remove login scope
                    $scope.removeScope();
                    // remove signIn scope
                    angular.element(document.getElementById('signUpController')).scope().removeScope();
                    // hide page1 (login and signin)
                    $("#page1").css("display", "none");
                    // request for user nickname to the servlet
                    $http.get("http://localhost:8080/webapp/getUserDetails") 
                        .success(function(response) {
                            // display nickname and photo
                            $("#nickname").text(response.nickname);
                            $("#profile img").attr("src",response.photo);
                            // display page2 - the channels
                            $(".page2").css("display", "block");
                            // connect to the websocket
                            connect(username);
                        });
                }
                // worng details
                else
                    // display error message
                    $(".signIn #retMsg").text(response);  
            });  
    };  

    // remove all scope varibale when changing pages
    $scope.removeScope=function(){
        for (var variable in $scope) {
            if (typeof $scope[variable] !== 'function' && variable.indexOf('$') == -1 && 
                variable.indexOf('$$') == -1 && variable!='this') {
                delete $scope[variable];
            }
        } 
        // initialize the message displayed
        $(".signIn #retMsg").text("");
    }
}]);

app.controller('signUpCtrl', ['$scope','$http', function($scope,$http) {
    // submit sign up form
    $scope.submit=function(){
        // for displaying error/information message in tooltip
        var valid=true;
        $(".signUp #retMsg").text("");
        /* the code is almost equel for all the input field. 
           we check if the input is valid. if yes in the tooltip written information about the field.
           if input is invaild we mark the icon in red, and display error message in the tooltip.
           we open show tooltip on focus, or the tooltip of first error field 
        */
        // if username not valid
        if(!$scope.username||$(".signUp [name='username']").val().length>10||$scope.username==""){
            // change icon color
            $('[name="username"] ~ span:first').css('color','red');
            // change tooltip color and display it
            $('[name="username"] ~ span:first').attr('data-original-title','up to 10 characters. required').tooltip('show');
            valid=false;
        }
        else{
            // update tooltip massage to information
            $('[name="username"] ~ span:first').attr('data-original-title','up to 10 characters. required');
            // change icon color to black
            $('[name="username"] ~ span:first').css('color','black'); 
        }
        if(!$scope.password||$(".signUp [name='password']").val().length>8||$scope.password==""){
            $('[name="password"] ~ span:first').css('color','red');
            // if here is the first error
            if(valid){
                // display tooltip
                $('[name="password"] ~ span:first').tooltip('show');
                valid=false;  
            }
        }
        else{
            $('[name="password"] ~ span:first').attr('data-original-title','up to 8 characters. required');
            $('[name="password"] ~ span:first').css('color','black'); 
        }
        if(!$scope.nickname||$(".signUp [name='nickname']").val().length>20||$scope.nickname==""||$scope.nickname.startsWith("@")){
            // we are now allow user nickename to start with '@'
            if($scope.nickname&&$scope.nickname.startsWith("@"))
                $('[name="nickname"] ~ span:first').attr('data-original-title','nickname cannot start with @');
            else
                $('[name="nickname"] ~ span:first').attr('data-original-title','public name, up to 20 characters. required');
            $('[name="nickname"] ~ span:first').css('color','red');
            if(valid){
                $('[name="nickname"] ~ span:first').tooltip('show');
                valid=false;  
            }
        }
        else{
            $('[name="nickname"] ~ span:first').attr('data-original-title','public name, up to 20 characters. required');
            $('[name="nickname"] ~ span:first').css('color','black'); 
        }
        if($(".signUp [name='description']").val().length>50){
            $('[name="description"] ~ span:first').css('color','red');
            if(valid){
                $('[name="description"] ~ span:first').tooltip('show');
                valid=false;  
            }
        }
        else{
            $('[name="description"] ~ span:first').css('color','black'); 
        }
        if(!valid)
           return;
        // url for sign up servlet
        url="signup/username/"+$scope.username+"/password/"+$scope.password+"/nickname/"+$scope.nickname;
        // if description or photo not filled we put defualt value
        if(!$scope.description)
            url+="/description//photo/";
        else
            url+="/description/"+$scope.description+"/photo/";
        if(!$scope.photo)
            // defult profile img
            url+="http://bonniesomerville.nz/wp-content/uploads/2015/08/profile-icon.png";
        else
            url+=$scope.photo;
        $http.get("http://localhost:8080/webapp/"+url) 
            .success(function(response) {
                if(response=="user added successfuly"){
                    // initialize textarea and input
                    $(".signUp").find("input[type=text], textarea, input[type=password]").val("");
                    // initialize tooltip messages
                    $('[name="username"] ~ span:first').attr('data-original-title','up to 10 characters. required');
                    $('[name="nickname"] ~ span:first').attr('data-original-title','public name, up to 20 characters. required');
                    // initilize scope variable
                    $scope.username=$scope.nickname=$scope.password=$scope.description=$scope.photo="";
                    $(".signUp #retMsg").text(response);
                }
                else if(response=="user already exist"){
                    // display username tooltip
                    $('[name="username"] ~ span:first').css('color','red');
                    $('[name="username"] ~ span:first').attr('data-original-title','username already taken').tooltip('show');
                }
                else if(response=="nickname already exist"){
                    $('[name="nickname"] ~ span:first').css('color','red');
                    $('[name="nickname"] ~ span:first').attr('data-original-title','nickname already taken').tooltip('show');
                }
            });
    };

    // bellow lines are related to the tooltips that displayed in signup form 
    // enable tooltip
    $('[data-toggle="tooltip"]').tooltip(); 
    // when user focus on input field, we display information about it in tooltip
    $(".signUp .form-group input , textarea").focus(function(){
        $($(this).next()).tooltip('show');  
    });
    // when user unfocus on input field we hide the tooltip
    $(".signUp .form-group input, textarea").on('blur', function hideInfo() {
        $($(this).next()).tooltip('hide');  
    });
    
    // remove all scope variables
    $scope.removeScope=function(){
        for (var variable in $scope) {
            if (typeof $scope[variable] !== 'function' && variable.indexOf('$') == -1 && 
                variable.indexOf('$$') == -1 && variable!='this') {
                delete $scope[variable];
            }
        } 
        $('[data-toggle="tooltip"]').css('color','black');
        $('[data-toggle="tooltip"]').tooltip('hide'); 
        $(".signUp #retMsg").text(""); 
    }
}]);
    
    // event when user write in description textarea
    $('[name="description"]').keydown(function(event) {
        // if len >50 and user did not press on delete
        if ($(this).val().length>=50 && !(event.which == 8||event.which == 46)) { 
            // not display the key pressed
            event.preventDefault();
        }
    });