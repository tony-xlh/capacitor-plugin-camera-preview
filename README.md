# capacitor-plugin-dynamsoft-camera-preview

A Capacitor plugin for camera preview using [Dynamsoft Camera Enhancer](https://www.dynamsoft.com/camera-enhancer/overview/).

[Online Demo](https://fastidious-squirrel-b4bc07.netlify.app/)

## Supported Platforms

* Web
* Android (WIP)
* iOS (WIP)

## Install

```bash
npm install capacitor-plugin-dynamsoft-camera-preview
npx cap sync
```

## API

<docgen-index>

* [`initialize()`](#initialize)
* [`getResolution()`](#getresolution)
* [`setResolution(...)`](#setresolution)
* [`getAllCameras()`](#getallcameras)
* [`getSelectedCamera()`](#getselectedcamera)
* [`selectCamera(...)`](#selectcamera)
* [`setScanRegion(...)`](#setscanregion)
* [`setZoom(...)`](#setzoom)
* [`setFocus(...)`](#setfocus)
* [`setDefaultUIElementURL(...)`](#setdefaultuielementurl)
* [`startCamera()`](#startcamera)
* [`stopCamera()`](#stopcamera)
* [`pauseCamera()`](#pausecamera)
* [`resumeCamera()`](#resumecamera)
* [`takeSnapshot()`](#takesnapshot)
* [`takePhoto()`](#takephoto)
* [`toggleTorch(...)`](#toggletorch)
* [`requestCameraPermission()`](#requestcamerapermission)
* [`addListener('onPlayed', ...)`](#addlisteneronplayed)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize()

```typescript
initialize() => Promise<void>
```

--------------------


### getResolution()

```typescript
getResolution() => Promise<{ resolution: string; }>
```

**Returns:** <code>Promise&lt;{ resolution: string; }&gt;</code>

--------------------


### setResolution(...)

```typescript
setResolution(options: { resolution: number; }) => Promise<void>
```

| Param         | Type                                 |
| ------------- | ------------------------------------ |
| **`options`** | <code>{ resolution: number; }</code> |

--------------------


### getAllCameras()

```typescript
getAllCameras() => Promise<{ cameras: string[]; }>
```

**Returns:** <code>Promise&lt;{ cameras: string[]; }&gt;</code>

--------------------


### getSelectedCamera()

```typescript
getSelectedCamera() => Promise<{ selectedCamera: string; }>
```

**Returns:** <code>Promise&lt;{ selectedCamera: string; }&gt;</code>

--------------------


### selectCamera(...)

```typescript
selectCamera(options: { cameraID: string; }) => Promise<void>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ cameraID: string; }</code> |

--------------------


### setScanRegion(...)

```typescript
setScanRegion(options: { region: ScanRegion; }) => Promise<void>
```

| Param         | Type                                                           |
| ------------- | -------------------------------------------------------------- |
| **`options`** | <code>{ region: <a href="#scanregion">ScanRegion</a>; }</code> |

--------------------


### setZoom(...)

```typescript
setZoom(options: { factor: number; }) => Promise<void>
```

| Param         | Type                             |
| ------------- | -------------------------------- |
| **`options`** | <code>{ factor: number; }</code> |

--------------------


### setFocus(...)

```typescript
setFocus(options: { x: number; y: number; }) => Promise<void>
```

| Param         | Type                                   |
| ------------- | -------------------------------------- |
| **`options`** | <code>{ x: number; y: number; }</code> |

--------------------


### setDefaultUIElementURL(...)

```typescript
setDefaultUIElementURL(url: string) => Promise<void>
```

| Param     | Type                |
| --------- | ------------------- |
| **`url`** | <code>string</code> |

--------------------


### startCamera()

```typescript
startCamera() => Promise<void>
```

--------------------


### stopCamera()

```typescript
stopCamera() => Promise<void>
```

--------------------


### pauseCamera()

```typescript
pauseCamera() => Promise<void>
```

--------------------


### resumeCamera()

```typescript
resumeCamera() => Promise<void>
```

--------------------


### takeSnapshot()

```typescript
takeSnapshot() => Promise<{ base64: string; }>
```

**Returns:** <code>Promise&lt;{ base64: string; }&gt;</code>

--------------------


### takePhoto()

```typescript
takePhoto() => Promise<{ base64: string; }>
```

**Returns:** <code>Promise&lt;{ base64: string; }&gt;</code>

--------------------


### toggleTorch(...)

```typescript
toggleTorch(options: { on: boolean; }) => Promise<void>
```

| Param         | Type                          |
| ------------- | ----------------------------- |
| **`options`** | <code>{ on: boolean; }</code> |

--------------------


### requestCameraPermission()

```typescript
requestCameraPermission() => Promise<void>
```

--------------------


### addListener('onPlayed', ...)

```typescript
addListener(eventName: 'onPlayed', listenerFunc: onPlayedListener) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                          |
| ------------------ | ------------------------------------------------------------- |
| **`eventName`**    | <code>'onPlayed'</code>                                       |
| **`listenerFunc`** | <code><a href="#onplayedlistener">onPlayedListener</a></code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### Interfaces


#### ScanRegion

measuredByPercentage: 0 in pixel, 1 in percent

| Prop                       | Type                |
| -------------------------- | ------------------- |
| **`left`**                 | <code>number</code> |
| **`top`**                  | <code>number</code> |
| **`right`**                | <code>number</code> |
| **`bottom`**               | <code>number</code> |
| **`measuredByPercentage`** | <code>number</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### onPlayedListener

<code>(result: { resolution: string; }): void</code>

</docgen-api>
