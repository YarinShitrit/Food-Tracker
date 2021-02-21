const functions = require("firebase-functions");
const admin = require("firebase-admin");
const { LogLevel } = require("@firebase/logger");
admin.initializeApp();




/**
 * Sending message to the new subscriber indicating the place total favorites
 */
exports.onPlaceSubscribed = functions.database
    .ref("places/{placeID}/subscribers/{userID}")
    .onCreate((_snapshot, context) => {
        const placeID = context.params.placeID;
        const userID = context.params.userID;
        console.log(`New subscriber to place ${placeID}, subscriber is ${userID}`)
        const totalFavoritesRef = admin.database().ref("/places/" + placeID + "/total_favorites");
        return totalFavoritesRef.transaction((currentFavorites) => {
            currentFavorites = (currentFavorites || 0)
            console.log(`retrieved place favorites for ${placeID}, current favorites: ${currentFavorites}`);
            const payload = {
                "notification": {
                    "title": "Place total favorites"
                },
                "data": {
                    "placeID": `${placeID}`,
                    "favorites": `${currentFavorites}`
                }
            }
            admin.database().ref("/users/" + userID + "/token").on("value", (snapshot) => {
                var token = snapshot.val()
                console.log("user token is " + token);
                admin.messaging().sendToDevice(token, payload) //sends update to place subscribers
                console.log(`Sent message to user ${userID}`)
            }, (err) => {
                console.log("Failed to get user token", err)
            });
        }).then(() => {
            console.log("Succsusfully finished transaction")
        })
    })


/**
     * increasing a place favorites after a user added it to favorites
     * @param {*} placeID the ID of place to incraese its total favorites
     */
exports.increasePlaceFavorites = function increasePlaceFavorites(placeID) {
    const totalFavoritesRef = admin.database()
        .ref("/places/" + placeID + "/total_favorites");
    totalFavoritesRef.transaction((currentFavorites) => {
        console.log(`Increasing place favorites for ${placeID}, current favorites: ${currentFavorites}`);
        const payload = {
            "notification": {
                "title": "Place total favorites increased"
            },
            "data": {
                "placeID": `${placeID}`,
                "favorites": `${(currentFavorites || 0) + 1}`
            }
        }
        admin.messaging().sendToTopic(placeID, payload) //sends update to place subscribers on favorites increase
        return (currentFavorites || 0) + 1;
    }).then(() => {
        console.log("Succssusflly increased place favorites");
    });
};

exports.decreasePlaveFavorites = function decreasePlaveFavorites(placeID) {
    const totalFavoritesRef = admin.database()
        .ref("/places/" + placeID + "/total_favorites");
    totalFavoritesRef.transaction((currentFavorites) => {
        if(currentFavorites == null){
            console.log("favorites is null")
            return null
        }
            console.log(`Decreasing place favorites for ${placeID}, current favorites: ${currentFavorites}`);
            const payload = {
                "notification": {
                    "title": "Place total favorites decreased"
                },
                "data": {
                    "placeID": `${placeID}`,
                    "favorites": `${currentFavorites - 1}`
                }
            }
            admin.messaging().sendToTopic(placeID, payload) //sends update to place subscribers on favorites decrease
            if (currentFavorites == 1) {
                return null;
            } else {
                return currentFavorites - 1;
            }
    
    }).then(() => {
        console.log("Succssusflly decreased place faovrites");
    });
};

exports.addPlace = functions.database
    .ref("/users/{userID}/favorite_places/{placeID}")
    .onCreate((snapshot, context) => {
        console.log("Place added for user ",
            context.params.userID,
            ", Place is ", snapshot.val()["name"]);
        exports.increasePlaceFavorites(snapshot.val()["place_id"]);
        // exports.subscribeUserToPlaceUpdates(context.params.userID,context.params.placeID);
    });

exports.subscribeUserToPlaceUpdates =
    function subscribeUserToPlaceUpdates(userID, placeID) {
        console.log("subsribeUserToPlaceUpdates() called")
        admin.database().ref("/users/" + userID + "/token").on("value", (snapshot) => {
            var token = snapshot.val()
            console.log("user token is " + token);
            admin.messaging().subscribeToTopic(token, placeID)
                .then((response) => {
                    console.log(`Successfully assigned user ${userID} to place ${placeID}`);
                }, (err) => {
                    console.log("Failed to subscribe user", err)
                });
        }, (err) => {
            console.log("Failed to get user token", err)
        });
    }

exports.deletePlace = functions.database
    .ref("/users/{userID}/favorite_places/{placeID}")
    .onDelete((snapshot, context) => {
        console.log("Place deleted for user ",
            context.params.userID,
            ", Place is ", snapshot.val()["name"]);
        return exports.decreasePlaveFavorites(snapshot.val()["place_id"]);
        // exports.unsubscribeUserToPlaceUpdates(context.params.userID, context.params.placeID);
    });

exports.unsubscribeUserToPlaceUpdates =
    function unsubscribeUserToPlaceUpdates(userID, placeID) {
        admin.database().ref("/users/" + userID + "/token").on("value", (snapshot) => {
            var token = snapshot.val()
            console.log("user token is " + token);
            admin.messaging().unsubscribeFromTopic(token, placeID)
                .then((response) => {
                    console.log(`Successfully unassigned user ${userID} to place ${placeID}`);
                }, (err) => {
                    console.log("Failed to unsubscribe user", err)
                });
        }, (err) => {
            console.log("Failed to get user token", err)
        });
    };
