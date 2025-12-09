import { auth } from "./firebase.js";
import { 
    createUserWithEmailAndPassword, 
    updateProfile, 
    sendEmailVerification 
} from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

// Function to translate Firebase error codes to friendly messages
const getFriendlyErrorMessage = (errorCode) => {
    switch (errorCode) {
        case 'auth/email-already-in-use':
            return 'This email address is already in use. Please sign in or use a different email.';
        case 'auth/invalid-email':
            return 'The email address format is invalid. Please check your email and try again.';
        case 'auth/weak-password':
            // Firebase default requires 6 characters minimum
            return 'The password is too weak. Please use a password with at least 6 characters.';
        case 'auth/operation-not-allowed':
            return 'Account creation is currently disabled. Please contact support.';
        default:
            // Fallback for unexpected errors
            return 'An unknown error occurred during sign up. Please try again.';
    }
};

document.getElementById("signupForm").addEventListener("submit", (e) => {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    console.log("Attempting sign up and email verification..."); 
    
    createUserWithEmailAndPassword(auth, email, password)
    .then(userCredential => {
        const user = userCredential.user;
        
        // 1. Update the user's profile with the username
        return updateProfile(user, { displayName: username })
            // 2. Send the verification email
            .then(() => sendEmailVerification(user)); 
    })
    .then(() => {
        // SUCCESS message remains the same
        alert("Account created successfully! We sent a verification email. Please check your INBOX AND SPAM FOLDER to verify your account before signing in.");
        window.location.href = "signin.html";
    })
    .catch(error => {
        // Get the friendly message based on the error code
        const errorMessage = getFriendlyErrorMessage(error.code);

        // Log the detailed Firebase error to the console for your own debugging
        console.error("Firebase Sign Up Error:", error);

        // Show the user the friendly message
        alert(`Sign Up Failed: ${errorMessage}`);
    });
});