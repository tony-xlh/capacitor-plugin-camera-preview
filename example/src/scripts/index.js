import '../styles/index.scss';
import { Capacitor } from '@capacitor/core';
import { CameraPreview } from "capacitor-plugin-dynamsoft-camera-preview";
import { BarcodeReader } from "dynamsoft-javascript-barcode";


console.log('webpack starterkit');

let onPlayedListener;
let reader;
let interval;
let decoding = false;
let captureBtn = document.getElementById("captureButton");
let zoominBtn = document.getElementById("zoominButton");
let zoomoutBtn = document.getElementById("zoomoutButton");
let startBtn = document.getElementById("startBtn");
let toggleTorchBtn = document.getElementById("toggleTorchButton");
startBtn.addEventListener("click",startCamera);
captureBtn.addEventListener("click",captureAndClose);
zoominBtn.addEventListener("click",zoomin);
zoomoutBtn.addEventListener("click",zoomout);
toggleTorchBtn.addEventListener("click",toggleTorch);

let torchStatus = false;

initialize();

async function initialize(){
  startBtn.innerText = "Initializing...";
  await CameraPreview.initialize();
  if (onPlayedListener) {
    await onPlayedListener.remove();
  }
  onPlayedListener = await CameraPreview.addListener('onPlayed', async (res) => {
    console.log(res);
    updateResolutionSelect(res.resolution);
    updateCameraSelect();
  });
  
  await CameraPreview.requestCameraPermission();
  await CameraPreview.setScanRegion({region:{left:10,top:20,right:90,bottom:65,measuredByPercentage:1}});
  await loadCameras();
  loadResolutions();
  await initDBR();
  startBtn.innerText = "Start Camera";
  startBtn.disabled = "";
}

async function initDBR() {
  BarcodeReader.engineResourcePath = "https://cdn.jsdelivr.net/npm/dynamsoft-javascript-barcode@9.3.0/dist/";
  BarcodeReader.license = "DLS2eyJoYW5kc2hha2VDb2RlIjoiMjAwMDAxLTE2NDk4Mjk3OTI2MzUiLCJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSIsInNlc3Npb25QYXNzd29yZCI6IndTcGR6Vm05WDJrcEQ5YUoifQ==";
  reader = await BarcodeReader.createInstance();
}

async function startCamera(){
  await CameraPreview.startCamera();
  toggleControlsDisplay(true);
  startDecoding();
}

function toggleControlsDisplay(show){
  if (show) {
    document.getElementsByClassName("home")[0].style.display = "none";
    document.getElementsByClassName("controls")[0].style.display = "";
  }else {
    document.getElementsByClassName("home")[0].style.display = "";
    document.getElementsByClassName("controls")[0].style.display = "none";
  }
}

async function captureAndClose(){
  stopDecoding();
  let result = await CameraPreview.takeSnapshot({quality:85});
  let base64 = result.base64;
  document.getElementById("captured").src = "data:image/jpeg;base64," + base64;
  await CameraPreview.stopCamera();
  toggleControlsDisplay(false);
}

async function loadCameras(){
  let cameraSelect = document.getElementById("cameraSelect");
  cameraSelect.innerHTML = "";
  let result = await CameraPreview.getAllCameras();
  let cameras = result.cameras;
  for (let i = 0; i < cameras.length; i++) {
    cameraSelect.appendChild(new Option(cameras[i], i));
  }
  cameraSelect.addEventListener("change", async function() {
    console.log("camera changed");
    let camSelect = document.getElementById("cameraSelect");
    await CameraPreview.selectCamera({cameraID:camSelect.selectedOptions[0].label});
  });
}

function loadResolutions(){
  let resSelect = document.getElementById("resolutionSelect");
  resSelect.innerHTML = "";
  resSelect.appendChild(new Option("ask 480P", 1));
  resSelect.appendChild(new Option("ask 720P", 2));
  resSelect.appendChild(new Option("ask 1080P", 3));
  resSelect.appendChild(new Option("ask 2K", 4));
  resSelect.appendChild(new Option("ask 4K", 5));
  resSelect.addEventListener("change", async function() {
    let resSelect = document.getElementById("resolutionSelect");
    let lbl = resSelect.selectedOptions[0].label;
    if (lbl.indexOf("ask") != -1) {
      let res = parseInt(resSelect.selectedOptions[0].value);
      await CameraPreview.setResolution({resolution:res});
    }
  });
}

async function updateResolutionSelect(newRes){
  let resSelect = document.getElementById("resolutionSelect");
  for (let index = resSelect.options.length - 1; index >=0 ; index--) {
    let option = resSelect.options[index];
    if (option.label.indexOf("got") != -1) {
      resSelect.removeChild(option);
    }
  }
  resSelect.appendChild(new Option("got "+newRes,"got "+newRes));
  resSelect.selectedIndex = resSelect.length - 1;
}

async function updateCameraSelect(){
  let cameraSelect = document.getElementById("cameraSelect");
  let selectedCamera = (await CameraPreview.getSelectedCamera()).selectedCamera;
  for (let i = 0; i < cameraSelect.options.length; i++) {
    if (cameraSelect.options[i].label === selectedCamera) {
      cameraSelect.selectedIndex = i;
      return;
    }
  }
}

async function zoomin(){
  await CameraPreview.setZoom({factor:2.5});
}

async function zoomout(){
  await CameraPreview.setZoom({factor:1.0});
}

async function toggleTorch(){
  try {
    let desiredStatus = !torchStatus;
    await CameraPreview.toggleTorch({on:desiredStatus});
    torchStatus = desiredStatus;   
  } catch (error) {
    alert(error);
  }
}

function startDecoding(){
  decoding = false;
  interval = setInterval(captureAndDecode,100);
}

function stopDecoding(){
  clearInterval(interval);
  decoding = false;
}

async function captureAndDecode(){
  if (reader === null) {
    return;
  }
  if (decoding === true) {
    return;
  }
  let results = [];
  let frame;
  let base64;
  decoding = true;
  try {
    if (Capacitor.isNativePlatform()) {
      let result = await CameraPreview.takeSnapshot({quality:85});
      base64 = result.base64;
      results = await reader.decodeBase64String(base64);
    } else {
      let result = await CameraPreview.takeSnapshot2();
      frame = result.frame;
      results = await reader.decode(frame);
    }  
  } catch (error) {
    console.log(error);
  }
  decoding = false;
  console.log(results);
  if (results.length>0){
    let dataURL;
    if (frame) {
      dataURL = frame.toCanvas().toDataURL('image/jpeg');
    }
    if (base64) {
      dataURL = "data:image/jpeg;base64," + base64;
    }
    stopDecoding();
    returnToHomeWithBarcodeResults(dataURL, results);
  }
}

async function returnToHomeWithBarcodeResults(dataURL, results) {
  document.getElementById("captured").src = dataURL;

  let ol = document.getElementById("barcodeResults");
  ol.innerHTML = "";
  results.forEach(result => {
    let item = document.createElement("li");
    item.innerText = result.barcodeFormatString+": "+result.barcodeText;
    ol.appendChild(item);
  });

  await CameraPreview.stopCamera();
  toggleControlsDisplay(false);
}
