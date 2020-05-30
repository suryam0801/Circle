'use-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref("/Notifications/{user_id}/{notify_id}").onWrite((change,context)=>{

    const user_id = context.params.user_id;
    const notify_id = context.params.notify_id;

	console.log("User ID : "+user_id+" | Notification ID :"+ notify_id);

	const getDeviceTokensPromise = admin.database()
          .ref(`/Users/${user_id}/token_id`).once('value');

	const state = admin.database()
          .ref(`/Notifications/${user_id}/${notify_id}/state`).once('value');
	
	const getFollowerProfilePromise = admin.auth().getUser(user_id);


	// The snapshot to the user's tokens.
      let tokensSnapshot;

      // The array containing all the user's tokens.
      let tokens;

    const results = await Promise.all([getDeviceTokensPromise, getFollowerProfilePromise]);
      tokensSnapshot = results[0];
      const follower = results[1];

     if (!tokensSnapshot.hasChildren()) {
        return console.log('There are no notification tokens to send to.');
      }
      console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
      console.log('Fetched follower profile', follower);

       const payload = {
        notification: {
          title: 'You have a new follower!',
          body: `${follower.displayName} is now following you.`,
          icon: follower.photoURL
        }
      };


      // Listing all tokens as an array.
      tokens = Object.keys(tokensSnapshot.val());
      // Send notifications to all tokens.
      const response = await admin.messaging().sendToDevice(tokens, payload);
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);

/*
	return admin.database().ref("/Notifications/"+user_id+notify_id).once('value',(snapshot) =>{

		const from_user_id = snapshot.val().from;

        const from_state = snapshot.val().state;

		const to_user_id = snapshot.val().notify_to;


        const from_data = admin.database().ref("/Users/"+from_user_id).get();
        
        const to_data = admin.firestore().ref("/Users/"+to_user_id).get();

        return Promise.all([from_data, to_data]).then(result =>{
			const from_name = result[0].val().firstName;

            const to_name = result[1].val().firstName;

            const token = result[1].val().token_id;

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
*/

});

