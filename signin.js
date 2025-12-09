import { auth } from "./firebase.js";
import { signInWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

document.getElementById("signinForm").addEventListener("submit", (e) => {
    e.preventDefault();

    const email = document.getElementById("signinEmail").value;
    const password = document.getElementById("signinPassword").value;

    console.log(`Attempting sign in for: ${email}`); // Debugging message
    
    signInWithEmailAndPassword(auth, email, password)
    .then(() => {
        alert("Login successful!");
        window.location.href = "dashboard.html";
    })
    .catch(error => {
        // CRUCIAL CHANGE: Log the detailed error to the console and show a message
        console.error("Firebase Sign In Error:", error);
        alert(`Login failed: ${error.message}`);
    });
});