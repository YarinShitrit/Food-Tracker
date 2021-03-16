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
        totalFavoritesRef.once("value", (snapshot) => {
            const totalFavorites = (snapshot.val() || 0)
            console.log(`retrieved place favorites for ${placeID}, current favorites: ${totalFavorites}`);
            const payload = {
                "data": {
                    "placeID": `${placeID}`,
                    "favorites": `${totalFavorites}`
                }
            }
            admin.database().ref("/users/" + userID + "/token").once("value", async (snapshot) => {
                var token = snapshot.val()
                console.log("user token is " + token);
                await admin.messaging().sendToDevice(token, payload).then(() => {
                    console.log(`Sent message to user ${userID}`)
                }) //sends update to place subscribers
            }, (err) => {
                console.log("Failed to get user token", err)
            });
        }, (err) => {
            console.log("Failed to get total favorites", err)
        });
    });


/**
     * increasing a place favorites after a user added it to favorites
     * @param {*} placeID the ID of place to incraese its total favorites
     */
exports.increasePlaceFavorites = async function increasePlaceFavorites(placeID) {
    console.log("increasePlaceFavorites() called");
    const totalFavoritesRef = admin.database().ref("/places/" + placeID + "/total_favorites");
    totalFavoritesRef.once("value", (snapshot) => {
        console.log(`Increasing place favorites for ${placeID}, current favorites: ${snapshot.val() || 0}`);
        snapshot.ref.set((snapshot.val() || 0) + 1, (err) => {
            if (err != null) {
                console.log("Failed to increase place favorites ->", err);
            }
            else {
                console.log("updating total favorites to ", (snapshot.val() || 0) + 1)
                const payload = {
                    "data": {
                        "placeID": `${placeID}`,
                        "favorites": `${(snapshot.val() || 0) + 1}`
                    }
                }
                admin.messaging().sendToTopic(placeID, payload).then(() => {
                    console.log("Sent message to users about increase")
                })//sends update to place subscribers on favorites increase
                return (snapshot.val() || 0) + 1
            }
        });
    });
};

exports.decreasePlaceFavorites = async function decreasePlaveFavorites(placeID) {
    console.log("decreasePlaceFavorites() called")
    const totalFavoritesRef = admin.database()
        .ref("/places/" + placeID + "/total_favorites");
    totalFavoritesRef.once("value", (snapshot) => {
        const value = snapshot.val()
        console.log(`Decreasing place favorites for ${placeID}, current favorites: ${value}`);
        const payload = {
            "data": {
                "placeID": `${placeID}`,
                "favorites": `${(value - 1)}`
            }
        }
        admin.messaging().sendToTopic(placeID, payload).then(() => {
            console.log("Sent message to users about decrease")
        }) //sends update to place subscribers on favorites decrease
        if (value == 1) {
            snapshot.ref.remove((err) => {
                if (err != null) {
                    console.log("Failed to remove place ->", err);
                }
                else {
                    console.log("Removed place")
                    return null
                }
            })
        }
        else {
            snapshot.ref.set(value - 1, (err) => {
                if (err) {
                    console.log("Failed to decrease place favorites ->", err);
                }
                else {
                    console.log("Decreased total favorites")
                    return value - 1;
                }
            })
        }
    })
}

exports.addPlace = functions.database
    .ref("/users/{userID}/favorite_places/{placeID}")
    .onCreate((snapshot, context) => {
        console.log(`Place added for user ${context.params.userID}, Place is ${snapshot.val()["name"]}`);
        exports.increasePlaceFavorites(context.params.placeID).then((snapshot) => {
            console.log("place increased value is ", snapshot)
            /*
            const payload = {
                "data": {
                    "placeID": `${context.params.placeID}`,
                    "favorites": `${(snapshot || 0) + 1}`
                }
            }
            admin.messaging().sendToTopic(context.params.placeID, payload).then(() => {
                console.log("Sent message to users about increase")
            })//sends update to place subscribers on favorites increase*/
        })

        // exports.subscribeUserToPlaceUpdates(context.params.userID,context.params.placeID);
    });

exports.deletePlace = functions.database
    .ref("/users/{userID}/favorite_places/{placeID}")
    .onDelete((snapshot, context) => {
        console.log("Place deleted for user ",
            context.params.userID,
            ", Place is ", snapshot.val()["name"]);
        exports.decreasePlaceFavorites(context.params.placeID)
            .then((snapshot) => {
                /*console.log("place decreased value is ", snapshot)
                const payload = {
                    "data": {
                        "placeID": `${context.params.placeID}`,
                        "favorites": `${(snapshot || 0)}`
                    }
                }
                admin.messaging().sendToTopic(context.params.placeID, payload).then(() => {
                    console.log("Sent message to users about decrease")
                })*/ //sends update to place subscribers on favorites decrease
            })
        // exports.unsubscribeUserToPlaceUpdates(context.params.userID, context.params.placeID);
    });

exports.subscribeUserToPlaceUpdates =
    function subscribeUserToPlaceUpdates(userID, placeID) {
        console.log("subsribeUserToPlaceUpdates() called")
        admin.database().ref("/users/" + userID + "/token").on("value", (snapshot) => {
            var token = snapshot.val()
            console.log("user token is " + token);
            admin.messaging().subscribeToTopic(token, placeID)
                .then(() => {
                    console.log(`Successfully assigned user ${userID} to place ${placeID}`);
                }, (err) => {
                    console.log("Failed to subscribe user", err)
                });
        }, (err) => {
            console.log("Failed to get user token", err)
        });
    }

exports.unsubscribeUserToPlaceUpdates =
    function unsubscribeUserToPlaceUpdates(userID, placeID) {
        admin.database().ref("/users/" + userID + "/token").on("value", (snapshot) => {
            var token = snapshot.val()
            console.log("user token is " + token);
            admin.messaging().unsubscribeFromTopic(token, placeID)
                .then(() => {
                    console.log(`Successfully unassigned user ${userID} to place ${placeID}`);
                }, (err) => {
                    console.log("Failed to unsubscribe user", err)
                });
        }, (err) => {
            console.log("Failed to get user token", err)
        });
    };
