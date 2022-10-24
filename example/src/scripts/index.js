import '../styles/index.scss';
import { CameraPreview } from "capacitor-plugin-camera-preview";

console.log('webpack starterkit');

//let cameraSelect = document.getElementById("cameraSelect");
//let resolutionSelect = document.getElementById("resolutionSelect");
//let closeButton = document.getElementById("closeButton");
//let zoominButton = document.getElementById("zoominButton");
//let zoomoutButton = document.getElementById("zoomoutButton");
let startBtn =  document.getElementById("startBtn");
startBtn.addEventListener("click",startCamera);

initialize();

async function initialize(){
  await CameraPreview.initialize();
  startBtn.disabled = "";
}

async function startCamera(){
  await CameraPreview.startCamera();
  startBtn.style.display = "none";
  document.getElementsByClassName("controls")[0].style.display = "";
}
