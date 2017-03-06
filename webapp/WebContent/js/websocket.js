

    //connect to webScoket, given username
    function connect(username) {  
        var wsUri = "ws://"+window.location.host+"/webapp/chat/"+username;
        websocket = new WebSocket(wsUri);
        websocket.onopen = function(evt){ 
        };
        websocket.onmessage = function(evt) {
            notify(evt.data);
        };
        websocket.onerror = function(evt) {
        };              
        websocket.onclose = function(evt) {
            websocket = null;
        };
    }

    // send message/request to the websocket
    function sendMessage(message) {
        if (websocket != null){
            websocket.send(message);
        }
    }

    /* notify will handle messages from the websocket
       message from the web socket look like: type {json object}
    */
    function notify(message) {
        // type is from index 0 to first space in message
        var type=message.substr(0,message.indexOf(' '));
        // message is from first space to the end
        message=message.substr(message.indexOf(' ')+1);
        // transfer the message from the websocket to javascript object
        message=JSON.parse(message);
        // get access to scope of page2
        var $scope = angular.element(document.querySelector('.page2')).scope();
        // we use $scope.$apply to make sure changes will be displayed in the scope
        $scope.$apply(function() {
            $scope.getLastTenMessages="false";
            // if we are in the bottom of the chat
            if ($('#chat')[0].scrollHeight - $('#chat').scrollTop() <= $('#chat').height()+35)
                // we use this variable to decide if we should change scrollbar position
                $scope.changePosition="true"; 
            else
              $scope.changePosition="false";
            // if we got all the user channels (when opening websocket)
            if(type=="channelList"){
                // call to function that will display all the channels in the sidebar menu
                $scope.addChannelsList(message);
                return;
            }
            // if we got message (and all its replys)
            if(type.startsWith("chatMsg")){
                /* we get the message and the replys as array of object,
                   and here we convert that to one object of main message,
                   that contain array of replys 
                */
                var messages=message;
                // message[0] is the main message
                message=message[0];
                // we add attribue 'replays'
                message["replays"]=[];
                var i=1, len=messages.length;
                for(i=1;i<len;i++){
                    // add every reply to the 'replays' attribue
                    message["replays"].push(messages[i]);
                }
                // if the message is already in the channelMessage (meaning there was comment)
                if($scope.getMsgIndexById(message.id)!=-1){
                    // delete the message from the channelMessages
                    $scope.channelMessage.splice($scope.getMsgIndexById(message.id), 1);
                }
                // add the message to the channel       
                $scope.channelMessage.push(message);
                /* the message we get from the websocket look like:
                   chatMsg'id of new added comment' [messages].
                */
                var messageAdded=type.substring(7); //contain new comment id
                // if the message had replys
                if(messageAdded!=message.id){
                    /* append icon the will mark the new added comment. 
                       we wait half a second until we display it */
                    setTimeout(function() {
                    $("#"+messageAdded +" .media-heading").append('<span style="color:red" class="glyphicon glyphicon-asterisk"'+ 
                    'aria-hidden="true" >'); },500);       
                }       
            }
            // if other user opened private chat with current user
            if(type=="openPrivateChannel"){
                // get the private channel name we need to display (user2 nickname)
                var displayName = $scope.convertPrivateName(message.channel,$("#nickname").text());
                // add the private channel to the sidebar list
                $scope.privateDiscussion.push({"channel":message.channel,"displayedName": displayName,
                                               "unseenMsgNumber":0,"notification":0});
            }
            // if the other user in the private chat unsubscribed from the chat
            if(type=="removePrivateChannel"){
                // get index of the private channel
                var index=$scope.getChannelIndexByName(message.channel,"private");
                // remove the private channel from the sidebar menu
                $scope.privateDiscussion.splice(index,1);
                // move to homepage
                $scope.channelName=""; 
                $scope.moveToChannel("");
            }
            // if we get unseenMsg/mention, or if we are not in the bottom of the chat
            if(type=="channelUnseenMsgUpdate"||$scope.changePosition=="false"){                     
                if(type=="channelUnseenMsgUpdate")
                    // add nubmer of uneenMsg and mention
                    $scope.updateNotification(message.channel,message.unseenMsgNumber,message.notification);
                else
                    // enlarge uneenMsg by 1
                    $scope.updateNotification(message.channelName,1,0);         
            }
        });
    }

    // log out from websocket
    function logout(){
        if(websocket==null)
            return;
        websocket.close();
    }
