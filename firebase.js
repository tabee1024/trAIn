import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

// Your actual Firebase config
const firebaseConfig = {
  apiKey: "AIzaSyC5SEFm2-s6Jb2FNz3sz-iiNfg4rDeRIvY",
  authDomain: "train-cd32c.firebaseapp.com",
  projectId: "train-cd32c",
  storageBucket: "train-cd32c.firebasestorage.app",
  messagingSenderId: "1084816731210",
  appId: "1:1084816731210:web:f488c39139f663bac0cab3",
  measurementId: "G-RW5VE33EJL"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);