import { PluginListenerHandle } from "@capacitor/core";

export interface CameraPreviewPlugin {
  initialize(options: { license: string }): Promise<void>;
  getResolution(): Promise<{resolution: string}>;
  setResolution(options: {resolution: number}): Promise<void>;
  getAllCameras(): Promise<{cameras: string[]}>;
  getSelectedCamera(): Promise<{selectedCamera: string}>;
  selectCamera(options: {cameraID: string; }): Promise<void>;
  setScanRegion(options: {region:ScanRegion}): Promise<void>;
  setZoom(options: {factor: number}): Promise<void>;
  setFocus(options: {x: number, y: number}): Promise<void>;
  toggleTorch(options: {on: boolean}): Promise<void>;
  startCamera(): Promise<void>;
  stopCamera(): Promise<void>;
  pauseCamera(): Promise<void>;
  resumeCamera(): Promise<void>;
  takeSnapshot(): Promise<{base64:string}>;
  takePhoto(): Promise<{base64:string}>;
  addListener(
    eventName: 'onPlayed',
    listenerFunc: onPlayedListener,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
  removeAllListeners(): Promise<void>;
}

export type onPlayedListener = (result:{resolution:string}) => void;

/**
 * measuredByPercentage: 0 in pixel, 1 in percent
 */
export interface ScanRegion{
  left: number;
  top: number;
  right: number;
  bottom: number;
  measuredByPercentage: number;
}

export enum EnumResolution {
  RESOLUTION_AUTO = 0,
  RESOLUTION_480P = 1,
  RESOLUTION_720P = 2,
  RESOLUTION_1080P = 3,
  RESOLUTION_2K = 4,
  RESOLUTION_4K = 5
}
