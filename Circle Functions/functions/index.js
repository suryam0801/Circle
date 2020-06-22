'use-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.firestore.document("Users/{user_id}/Notifications/{notify_id}").onWrite((change,context)=>{

    const user_id = context.params.user_id;
    const notify_id = context.params.notify_id;

    console.log("User ID : "+user_id+" | Notification ID :"+ notify_id);


    return admin.firestore().collection("Users").doc(user_id).collection("Notifications").doc(notify_id).get().then(queryResult =>{

        const from_user_id = queryResult.data().from;

        const from_state = queryResult.data().state;

        const from_data = admin.firestore().collection("Users").doc(from_user_id).get();
        
        const to_data = admin.firestore().collection("Users").doc(user_id).get();

        return Promise.all([from_data, to_data]).then(result =>{

            const from_name = result[0].data().name;

            const to_name = result[1].data().name;

            const token = result[1].data().token_id;

            console.log("From  : "+from_name+" | To :"+ to_name +" | State : " +from_state + " | Token Id :" +token );

            const payload = {
                notification : {
                    title : "Notification From : " + from_name,
                    body : "Application status : " + from_state,
                    icon : "default"
                }
            }; 

            console.log("Payload :"+payload);

             

            return admin.messaging().sendToDevice(token,payload).then(result =>{

                console.log("Notification Sent!!!");
                return payload;

            });
            
        });
        

    });
   
});

//Broadcast


exports.sendBroadcastNotification = functions.firestore.document("Users/{user_id}/BroadcastNotification/{notify_id}").onWrite((change,context)=>{

    const user_id = context.params.user_id;
    const notify_id = context.params.notify_id;

    console.log("User ID : "+user_id+" | BroadcastNotification ID :"+ notify_id);


    return admin.firestore().collection("Users").doc(user_id).collection("BroadcastNotification").doc(notify_id).get().then(queryResult =>{

        const from_user_id = queryResult.data().creatorId;

        const from_state = queryResult.data().state;

        const circle_name = queryResult.data().circleName;

        const from_data = admin.firestore().collection("Users").doc(from_user_id).get();
        
        const to_data = admin.firestore().collection("Users").doc(user_id).get();

        return Promise.all([from_data, to_data]).then(result =>{

            const from_name = result[0].data().name;

            const to_name = result[1].data().name;

            const token = result[1].data().token_id;

            console.log("From  : "+from_name+" | To :"+ to_name +" | State : " +from_state + " | Token Id :" +token );

            const payload = {
                notification : {
                    title : "Notification From : " + circle_name,
                    body : " A new Broadcast added by : " + from_name,
                    icon : "default"
                }
            }; 

            console.log("Payload :"+payload);

             

            return admin.messaging().sendToDevice(token,payload).then(result =>{

                console.log("Notification Sent!!!");
                return payload;

            });
            
        });
        

    });
   
});