
app.controller('Ctrl', ['$scope','$http', function($scope,$http) {
    //variables for scrollbar position:
    $scope.previousHeight=0;
    $scope.getLastTenMessages="true";
    $scope.changePosition="true";
    
    $scope.publicDiscussion=[]; // save list of public discussion
    $scope.privateDiscussion=[]; // save list of private disussion
    $scope.channelMessage=[]; // hold current channel messages
    $scope.commentTo={}; // we using this variable for reply modal
    $scope.queryResult=[]; // var for search modal
    $scope.channelName=""; // current channel name

    // function that create private channel, called when user click on nickname
    $scope.createPrivateChat=function(user){
    // if user clicked on his name we dont open private channel
        if(user==$("#nickname").text()){
            return;
        }
        /* send requst to create private channel to servlet.
        the servlet return object that contains channel name in success
        */    
        var url="creatPrivateChannel/"+user;
        $http.get("http://localhost:8080/webapp/"+url) 
        .success(function(response) {
            // if channel is already exist
            if(!(typeof response =='object'))
                return;
            //call to function that add the private channel to the sidebar menu
            $scope.addPrivateChannel(response.channelName);
        });     
    }
    
    // create new public channel. called when user click submit in create channel modal
    $scope.createNewChannel=function(){
        //if the user clicked "submit" but didn't enter name
        if($scope.channelname==""){
            //display error msg
            $scope.msgDisplay="please enter channel name";
            return;
        }
        // we do not allow channel name to start with "@"
        if($scope.channelname.startsWith("@")){
            $scope.msgDisplay="channel name can't start with @";
            return;
        }
        // channel name up to 30 characters
        if($scope.channelname.length>30){
            $scope.msgDisplay="channel name length up to 30 characters";
              return;
        }
        // channel description up to 500 characters
        if($scope.description&&$scope.description.length>500){
            $scope.msgDisplay="description length up to 500 characters";
            return;
        }
        // url for sending request for create channel to servlet
        var url="creatchannel/channelname/"+ $scope.channelname +"/description/"+ $scope.description;
        // servlet return error/success message
        $http.get("http://localhost:8080/webapp/"+url) 
            .success(function(response) {
                //display the message from the servlet
                $scope.msgDisplay=response;
                //if channel added we call function that add the channel to the sidebar menu
                if(response.startsWith("channel added successfully")){
                    $scope.addPublicChannel($scope.channelname);    
            }
        });
    }
     
    // notify the websocket about new added chat, and add it to channelList in the sidebar menu
    $scope.addPublicChannel=function(channelName){
        // tell the web socket to open new channel listener
        $scope.notifyAddChannel(channelName);
        // we add the channel to the sidebar menu
        $scope.publicDiscussion.push({"channel":channelName,"unseenMsgNumber":0,"notification":0});
    }

    $scope.addPrivateChannel=function(channelName){
        // tell the web socket to open new channel listener
        $scope.notifyAddChannel(channelName);
        // convert private channel name from "@nickname1@nickname2@" to "nickname" 
        displayName=$scope.convertPrivateName(channelName, $("#nickname").text());
        // add the channel to private channel list at the sidebar
        $scope.privateDiscussion.push({"channel":channelName,"displayedName": displayName,
                                       "unseenMsgNumber":0,"notification":0});
        
    }
 
    /* when we first login to websocket this function display the channels list
       the parameter 'message' is the list we got from the websokcet, in json */
    $scope.addChannelsList=function(message){
        var len=message.length;
        var privateChannels=[];
        var publicChannels=[];
        var c;
      // iterate on the channels list we got from the websocket
      for(c=0;c<len;c++){
          // if this channel is private
          if(message[c].channel.startsWith("@")){
              // convert @nickname1@nickname2@ to "nickname"
              var name=$scope.convertPrivateName(message[c].channel,$("#nickname").text());
              // set attribute that save the name should be displayed in the list
              message[c]["displayedName"]=name;
              // add the channel to the list
              privateChannels.push(message[c]);
          }
          // public channel
          else{
              // when channel name has spaces, in the database space="%20", so we replace them with " "
              message[c].channel= message[c].channel.split("%20").join(" ");
              // name to display int the siderbar
              message[c]["displayedName"]=message[c].channel;
              // add the channel to the list
              publicChannels.push(message[c]);
          }
      }
      // update channels list in the sidebar menu
      $scope.publicDiscussion= publicChannels;
      $scope.privateDiscussion= privateChannels;
    }

    //search for channel, in modal search
    $scope.search=function(){
        // if user didn't enter query
        if($scope.query=="")
            return;
        // get the queryType, by finding the active class in the navbar
        var queryType = $(".nav-justified li[class*='active']").attr('id');
        // we update the url to the servlet according to the type
        if(queryType=="byName")
            var url="search/channelname/"+$scope.query;
        else
            var url="search/nickname/"+$scope.query;
        // send request to the servlet
        $http.get("http://localhost:8080/webapp/"+url) 
            .success(function(response) {
              // update scope query result
              $scope.queryResult=response;            
            });
    }

    // subscribe to public channel
    $scope.subscribe=function(x){ 
        // send request to the servlet to subscribe the channel
        var url="subscribe/channelname/"+x;
        $http.get("http://localhost:8080/webapp/"+url) 
            .success(function(response) {
                if(response.startsWith("success")){
                    // we add the channel to the list
                    $scope.addPublicChannel(x);
                    // display message
                    $scope.msgDisplay="user subscribed successfuly";  
                }
                // if subscribe failed it mean that the user already subscribed
                else
                    $scope.msgDisplay="user already subscribed to this channel";
            });
    }

    // unsubscribe for both public and private channels
    $scope.unsubscribe=function(){
        // save the channel we want to unsubscribe from
        var currentChannel=$scope.channelName;
        // move to home page
        $scope.moveToChannel("");
        // send request to the servlet to unsubscribe
        var url="unsubscribe/channelname/"+currentChannel;
        $http.get("http://localhost:8080/webapp/"+url) 
            .success(function(response) {
            });
        // if we need to remove public channel
        if(!(currentChannel.startsWith("@"))){
            // get index of the channel in the $scope.publicDiscusstion list
            var index=$scope.getChannelIndexByName(currentChannel,"public");
            // remove the channel from the list
            $scope.publicDiscussion.splice(index,1);
        }
        // if we need to remove private channel
        else{
            // we send request to websocket, that will send "remove" message to the other user
            var sendMsg= {"type":4,"channelName":currentChannel};
            sendMsg=JSON.stringify(sendMsg);
            sendMessage(sendMsg);
            // get index of the channel in the $scope.privateDiscusstion list
            var index=$scope.getChannelIndexByName(currentChannel,"private");
             // remove the channel from the list
            $scope.privateDiscussion.splice(index,1);
        }     
    }

    // move to channel that the user clicked on
    $scope.moveToChannel=function(channelName){
        // if we are not in the homepage
        if( $scope.channelName!=""){
            // we notify the websocket that we exit the current channel
            $scope.notifyMoveToChannel($scope.channelName,"false");
        }
        /* hold the range of message we request from the server (every time 10 messages).
           range i meaning we want the i+10 last messages */
        $scope.messageRange=0; 
        // when messages sent we will move the scrollbar to the bottom of the chat
        $scope.changePosition="true";
        // remove all messages of previous channel
        $scope.channelMessage=[];
        // if need to move to private channel
        if(channelName.startsWith("@")){
            // we get the channel index in the list
            var index=$scope.getChannelIndexByName(channelName);
            // set the header of the chat
            $("#channelNameHeader a").text($scope.privateDiscussion[index].displayedName);
        }
        else
            // set the header of the chat
            $("#channelNameHeader a").text(channelName);
        // update channel name
        $scope.channelName=channelName;
        // if we want to move to homepage
        if(channelName==""){
            // not to display unsubscribe option
              $("#unsubscribe").css("display","none");
            // remove the active channel in the list
              $('.channels').removeClass('active');
              return;
        }
        // display unsubsribe option in the channel header
        $("#unsubscribe").css("display","block");
        // update unseenMsg and mention to 0
        $scope.updateNotification(channelName,0,0);
        // notify the websocket that we active in the channel
        $scope.notifyMoveToChannel(channelName,"true");
        // display last ten messages
        $scope.displayMessages(channelName);
    }
    
    /* display last ten messages.
       IMPORTANT: we display last ten main messages, and all the replys to it.
    */
    $scope.displayMessages=function(channelName){
        // request from sevlet messages in range of [messageRange, messeageRange+10)
        var url="lastTenMsgs/channelname/"+channelName+"/range/"+$scope.messageRange;
        $http.get("http://localhost:8080/webapp/"+url) 
            .success(function(response) {
                // messages will hold all the main messages, and their replys
                var messages=[];
                var len=response.length;
                var i;
                for(i=0;i<len;i++)
                {
                    // if main message, mean the message is not reply (replyTo="-1")
                    if(response[i].replyTo=="-1"){
                        // we add attribue - array of replys
                        response[i]["replays"]=[];
                        var j=i+1;
                        // we add all the replys, until we arrive to new main message
                        while(j<len&&response[j].replyTo!="-1"){
                            // add the replay to the array
                            response[i]["replays"].push(response[j]);
                            j++;
                        }
                        // add the message to messages array
                        messages.push(response[i]);
                        i=j-1;
                    }
                }
                // update range of messages for next time
                $scope.messageRange+=10;
                // we add the messages to scope channelMessage
                $scope.channelMessage=messages.concat($scope.channelMessage);         
            });
    }

    // open reply modal when user click on reply icon. parameter - the message to comment on
    $scope.openReplyModal=function(x){
        // save the message, so we can know the nickname
        $scope.commentTo=x;
        /* send request to server, to check if the author of the message is subscribed to the channel,
           if yes the user can reply to the message
         */
        var url="isSubscribed/nickname/"+x.fromUser+"/channelname/"+$scope.channelName;
        $http.get("http://localhost:8080/webapp/"+url) 
            .success(function(response) {
                if(response.startsWith("true")){
                   // show modal
                   $('#replay').modal('show'); 
                   // add @ with the nickname of the user we comment to
                   $("#replayContent").val("@"+x.fromUser +" ");
                }
            });        
    }

    // function that send the replay to the websocket, called when user click "submit" in the reply modal
    $scope.replay=function(){
        // if length of message bigger than 500 characters
        if(replayContent.value&&replayContent.value.length>500){
            $scope.msgDisplay="up to 500 charaters";
            return;
        }
        // send the reply to websocket
        $scope.notifyOnMessage($scope.channelName, replayContent.value, $scope.commentTo.id);
        // hide modal
        $('#replay').modal('toggle');
    }
    
    // log out from the website
    $scope.logOut=function(){
        // if we are not in the homepage
        if($scope.channelName!=""){
            // notify the websocket to close the current chat
            $scope.notifyMoveToChannel($scope.channelName,"false");
        }
        // send request to servelt to sign out
        $http.get("http://localhost:8080/webapp/signout") 
            .success(function(response) {
                // hide page2 - the channels
                $(".page2").css("display", "none");
                // assign defult values to scope variables
                $scope.moveToChannel("");
                $scope.publicDiscusstion=[];
                $scope.privateDiscusstion=[];
                // log out from websocket
                logout();
                // display page1 - the log in page
                $("#page1").css("display", "block");
            });    
    }

    // function that will be called when the ng-repeat finished to load all the message
    $scope.$on('ngRepeatFinished', function(ngRepeatFinishedEvent) {
        // if we need to move the scrollbar down
        if($scope.changePosition=="true")
            // we move to the bottom of the chat
            $('#chat').scrollTop($('#chat').prop('scrollHeight'));
    });
 
    // change scrollbar position if need to
    $scope.changeScrollbarPosition=function(){
        // if we dont need to change current position, but we add messages   
        if($scope.previousHeight!=0&&$scope.getLastTenMessages=="true"&&$scope.changePosition=="false"){
            // we updated the scrollbar to remain in the same place, using previous height
            $('#chat').scrollTop($('#chat')[0].scrollHeight-$scope.previousHeight);
        }
    }

    // notify websocket when new channel created (private or public)
    $scope.notifyAddChannel=function(channelName){
        // every message to the websocket start with type of the message
        var sendMsg= {"type":3,"channelName":channelName};
        //convert to json
        sendMsg=JSON.stringify(sendMsg);
        //function that send message to the websocket
        sendMessage(sendMsg);
    } 

    // send message/reply to the websocket
    $scope.notifyOnMessage=function(channelName, content, replyTo){
        /* send to websocket the channelName, message content and the id of the message the user comment on
           (replyTo=-1 if not reply)
        */
        var sendMsg={"type":2,"channelName":channelName,"message":content,"replyTo": replyTo};
        sendMsg=JSON.stringify(sendMsg);
        sendMessage(sendMsg);
    }

    // notify the web socket about moving to channel or closing channel. open parameter can get false/true
    $scope.notifyMoveToChannel=function(channelName, open){
        // send to web socket type of request and channel to open/close
        var sendMsg= {"type":1,"opened":open,"channelName":channelName};
        sendMsg=JSON.stringify(sendMsg);
        sendMessage(sendMsg);
    }

    /* we save private channel in database like this: "@nickname1@nickname2@",
        therefore when dispaying private channel we need to convert the name that returned from the server
    */
    $scope.convertPrivateName=function(channelName,nickname){
        //remove all @
        channelName=channelName.split("@").join("");
        //remove user nickname (so in channelName will be the nickname of the other user)
        channelName =channelName.replace(nickname, '');
        return channelName;

    }

    // return index of message in the $scope.channelMessage
    $scope.getMsgIndexById=function(id){
        for(i=0;i<$scope.channelMessage.length;i++){
            if($scope.channelMessage[i].id==id){
                return i;
            }
        }
        return -1;
    }

    /* return index of channel in private or public chat. type parameter - public or private
       we assumed that channel name is uniqe */
    $scope.getChannelIndexByName=function(channelName, type){
        var i;
        if(type=="public"){
            var len=$scope.publicDiscussion.length;
            for(i=0;i<len;i++){
                if($scope.publicDiscussion[i]["channel"]==channelName)
                    return i;
            }
        }
        // type = private
        else{
            var len=$scope.privateDiscussion.length;
            for(i=0;i<len;i++){
                if($scope.privateDiscussion[i]["channel"]==channelName)
                    return i;
            }
        }
        return -1;
    }
    
    // update number of unseenMsg and mentions
    $scope.updateNotification=function(channelName,unseenMsg,mention){
        // meaning we need to update to 0
        if(unseenMsg==0&&mention==0){
            // get index of channel in public channels
            var channelIndex=$scope.getChannelIndexByName($scope.channelName,"public");
            // if the channel is public
            if(channelIndex!=-1){
                // update numbers to 0
                $scope.publicDiscussion[channelIndex]["unseenMsgNumber"]=0;
                $scope.publicDiscussion[channelIndex]["notification"]=0;
                return;
            }
            // get index of channel in private channels
            channelIndex=$scope.getChannelIndexByName($scope.channelName,"private");  
            if(channelIndex!=-1){
                // update numbers to 0
                $scope.privateDiscussion[channelIndex]["unseenMsgNumber"]=0;
                $scope.privateDiscussion[channelIndex]["notification"]=0;
                return;
            }
            return;
        }
        // exactly like above, except that here we need to enlarge number of notification 
        if(channelName!=""){
            var channelIndex=$scope.getChannelIndexByName(channelName,"public");
            if(channelIndex!=-1){
                $scope.publicDiscussion[channelIndex]["unseenMsgNumber"]+=unseenMsg;
                $scope.publicDiscussion[channelIndex]["notification"]+=mention;
                return;
            }
            channelIndex=$scope.getChannelIndexByName(channelName,"private");  
            $scope.privateDiscussion[channelIndex]["unseenMsgNumber"]+=unseenMsg;
            $scope.privateDiscussion[channelIndex]["notification"]+=mention;
            return;
        }
    }

    /* hide or display comments
       we display bottom or top arrow */
    $scope.commentsHide=function(mainId, replyId){
        var replyIndex; // index of reply in main message
        var index=$scope.getMsgIndexById(mainId); //index of main message
        var mainReplys=$scope.channelMessage[index].replays; // main message replys
        var r=0;
        var action="";
        // search index of reply in array of replys
        for(r=0;r<mainReplys.length;r++){
            if(mainReplys[r].id==replyId){
                replyIndex=r;
                r++;
                break;
            }
        }
        // if there is bottom arrow - and user clicked on it we need to hide comments
        if($("#"+mainReplys[replyIndex].id).find('span:first').hasClass('glyphicon-triangle-bottom')) {
            action="hide";
            $("#"+mainReplys[replyIndex].id).find('span:first').removeClass('glyphicon-triangle-bottom');
            // add top arrow class (icon)
            $("#"+mainReplys[replyIndex].id).find('span:first').addClass('glyphicon-triangle-top');
        }
        // else we need to display comments
        else{
            action="show";
            $("#"+mainReplys[replyIndex].id).find('span:first').removeClass('glyphicon-triangle-top');
            $("#"+mainReplys[replyIndex].id).find('span:first').addClass('glyphicon-triangle-bottom');
        }
        // pass over all the comments of the reply
        for(;r<mainReplys.length;r++){
            if(mainReplys[r].replyTo==mainReplys[replyIndex].replyTo)
                break;
            else{
                if(action=="hide")
                    $("#"+mainReplys[r].id).css('display','none');
                else
                    $("#"+mainReplys[r].id).css('display','block');
            }
        } 
    }

    /* when user wnat to hide/display main message comment.
       very similar to above function */
    $scope.commentsHideAll=function(mainId){
        var index=$scope.getMsgIndexById(mainId); // index of message
        var mainReplys=$scope.channelMessage[index].replays; // replys of the message
        var r=0;
        var action="";
        // if need to hide
        if($("#"+mainId).find('span:first').hasClass('glyphicon-triangle-bottom')) {
            action="hide";
            $("#"+mainId).find('span:first').removeClass('glyphicon-triangle-bottom');
            $("#"+mainId).find('span:first').addClass('glyphicon-triangle-top');
        }
        else{
            action="show";
            $("#"+mainId).find('span:first').removeClass('glyphicon-triangle-top');
            $("#"+mainId).find('span:first').addClass('glyphicon-triangle-bottom');
        }
        // hide/ display all the comments
        for(r=0;r<mainReplys.length;r++){
            if(action=="hide")
                $("#"+mainReplys[r].id).css('display','none');
            else
                $("#"+mainReplys[r].id).css('display','block');
        }
    }
}]);

    // event - input in message text area
    $(document).on('input', '#message', function () {
        /* update the height according to the scroll height - the text area enlarge if 
           new line begin. outerHeight - #message initial height */
        $(this).outerHeight(45).outerHeight(this.scrollHeight); 
        // write message is container of message, so when message enlarge we enlarge the container
        $('#writeMessage').outerHeight(90).outerHeight(this.scrollHeight+45);
        // we reduce chat height
        $('#chat').css("bottom",this.scrollHeight + 45 );
        // keep the scrollbar of chat in the same place
        $('#chat').scrollTop($('#chat').scrollTop()) ;
    }); 
    
    // event - input in description text area (in create channel modal)
    $(document).on('input', '#description', function () {
        // enlarge text area when we start new line
        $(this).outerHeight(100).outerHeight(this.scrollHeight); 
    });
 
    // event - input in replayContent text area (in reply modal)
    $(document).on('input', '#replayContent', function () {
        // enlarge text area when we start new line
        $(this).outerHeight(70).outerHeight(this.scrollHeight); 
    }); 

    // when user write in message text area
    $("#message").keydown(function(event) {
        // if key="enter"
        if (event.which == 13) {
            event.preventDefault();
            // if user write nothing or length>500
            if(!message.value||message.value==""||(message.value&&message.value.length>500))
                return;
            // get access to page2 scope
            var $scope = angular.element(document.querySelector('.page2')).scope();
            // if we are in homepage, we do not send the message to the server
            if($scope.channelName==""){
                $("#message").val("");
                return;
            }
            // send the message to websocket
            $scope.notifyOnMessage($scope.channelName,message.value,-1);
            // empty message text area
            $("#message").val("");
            // those three line return the chat and the message text area to their regular size 
            $("#message").outerHeight(45).outerHeight(45);
            $('#writeMessage').height(90);
            $('#chat').css("bottom",90 );
        }
        // length of message 
        var len = $(this).val().length;
        // if len >500 and user did not press on delete
        if (len >= 500 && !(event.which == 8||event.which == 46)) { 
            // not display the key pressed
            event.preventDefault();
        }
    });
    
    // when user write in reply text area or in description
    $("#replayContent, #description").keydown(function(event) {
        // length of message 
        var len = $(this).val().length;
        // if len >500 and user did not press on delete
        if (len >= 500 && !(event.which == 8||event.which == 46)) { 
            // not display the key pressed
            event.preventDefault();
        }
        var leftchars=500-len;
        // display how many character left
        $("#replay #leftChar, #createChannel #leftChar").text("("+leftchars+")");
    });


    //  event for clicking on navbar tab 
    $('.nav-justified li').on('click', function(){
        // get access to scope of page2
        var $scope = angular.element(document.querySelector('.page2')).scope();
        // change scope and save changes
        $scope.$apply(function() {
            // we initialize query and result
            $scope.query="";
            $scope.queryResult=[];
            $scope.msgDisplay="";
        });
        // remove class active 
        $('.nav-justified li').removeClass('active');
        // add the clicked tab 'active' class
        $(this).addClass('active');
    });

    // event when modal close
    $(".modal").on("hidden.bs.modal", function(){ 
        // get access to scope of page2
        var $scope = angular.element(document.querySelector('.page2')).scope();
        // change scope and save changes
        $scope.$apply(function() {
            // initialize scope variable
            $scope.queryResult=[];
            $scope.msgDisplay="";
        });
        // clean textarea and input
        $('.modal-body').find('textarea,input').val('');
        // initialize length of textarea to their oginal length
        $("#description").outerHeight(100).outerHeight(100);
        $("#replayContent").outerHeight(70).outerHeight(70);
        // clear left characters counter
        $("#replay #leftChar, #createChannel #leftChar").text("(500)");
    });

    // when user click on channel in the sidebar menue, we add "active" class to the channel
    $(document).on("click", ".channels", function() {
        // remove the previous channel that was active
        $('.channels').removeClass('active');
        // add active class to current clicked channel
        $(this).addClass('active');
    });


    /* used for waiting until we finish displaying data in ng-repeat, and than scroll down.
       we used this code from stackoverflow replys.
    */
    app.directive('onFinishRender', function ($timeout) {
        return {
            // the directive can only be invoked by attributes
            restrict: 'A',
            link: function (scope, element, attr) {
                // if we are on the last element in the ng-repeat
                if (scope.$last === true) {
                    // when we finished loading ng-repeat elemnts
                    $timeout(function () {
                        /* we call ngRepeatfinished event, and do our function there. 
                        in the function we move down the scrollbar if needed */
                        scope.$emit('ngRepeatFinished');
                    });
                }
            }
        }
    });

    // event - when scrollbar changes position
    $( "#chat" ).scroll(function() {
        // access to scope of page2
        var $scope = angular.element(document.querySelector('.page2')).scope();
        // if the scrollbar in the top of the chat
        if($('#chat').scrollTop()==0){
            // change scope var
            $scope.$apply(function() {
                // if we are on the top, but we haven't displayed first 10 messages yet 
                if( $scope.messageRange<=0)
                    return;
                $scope.getLastTenMessages="true"; // need to display 10 message
                $scope.changePosition="false"; // need to stay at the same position
                // save current scrollbar height, so later we can use it to stay in current position
                $scope.previousHeight=$('#chat')[0].scrollHeight;
                // call function that will get 10 messages from servlet and will display it
                $scope.displayMessages($scope.channelName);
            });
        }
        // if we in the bottom of the chat
        else if ($('#chat')[0].scrollHeight - $('#chat').scrollTop() <= $('#chat').height()+35)
        {
            $scope.$apply(function() {
                // update the notification to 0
                $scope.updateNotification("",0,0);
            });
        }  
    });

