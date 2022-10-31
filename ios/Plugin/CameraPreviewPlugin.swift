import Foundation
import Capacitor
import DynamsoftCameraEnhancer
/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CameraPreviewPlugin)
public class CameraPreviewPlugin: CAPPlugin {
    var dce:DynamsoftCameraEnhancer! = nil
    var dceView:DCECameraView! = nil
    @objc func initialize(_ call: CAPPluginCall) {
        // Initialize a camera view for previewing video.
        DispatchQueue.main.sync {
            dceView = DCECameraView.init(frame: (bridge?.viewController?.view.bounds)!)
            self.webView!.superview!.insertSubview(dceView, belowSubview: self.webView!)
            dce = DynamsoftCameraEnhancer.init(view: dceView)
            dce.setResolution(EnumResolution.EnumRESOLUTION_720P)
        }
        call.resolve()
    }
    
    @objc func startCamera(_ call: CAPPluginCall) {
        makeWebViewTransparent()
        if dce != nil {
            DispatchQueue.main.sync {
                dce.open()
                triggerOnPlayed()
            }
        }else{
            call.reject("DCE not initialized")
            return
        }
        call.resolve()
    }
    
    func makeWebViewTransparent(){
        DispatchQueue.main.async {
           self.bridge?.webView!.isOpaque = false
           self.bridge?.webView!.backgroundColor = UIColor.clear
           self.bridge?.webView!.scrollView.backgroundColor = UIColor.clear
       }
    }
    func restoreWebViewBackground(){
        DispatchQueue.main.async {
           self.bridge?.webView!.isOpaque = true
           self.bridge?.webView!.backgroundColor = UIColor.white
           self.bridge?.webView!.scrollView.backgroundColor = UIColor.white
       }
    }
    
    @objc func toggleTorch(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            DispatchQueue.main.sync {
                if call.getBool("on", true){
                   dce.turnOnTorch()
                } else{
                   dce.turnOffTorch()
                }
            }
            call.resolve()
        }
    }
    
    @objc func stopCamera(_ call: CAPPluginCall) {
        restoreWebViewBackground()
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            DispatchQueue.main.sync {
                dce.close()
            }
            call.resolve()
        }
    }
    
    @objc func resumeCamera(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            dce.resume()
            call.resolve()
        }
    }
    
    @objc func pauseCamera(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            dce.pause()
            call.resolve()
        }
    }
    
    @objc func setResolution(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            let res = call.getInt("resolution") ?? -1
            NSLog("Resolution: %d", res)
            
            if res != -1 {
                let resolution = EnumResolution.init(rawValue: res)
                dce.setResolution(resolution!)
                triggerOnPlayed()
            }
            call.resolve()
        }
    }
    
    @objc func getResolution(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            var ret = PluginCallResultData()
            let res = dce.getResolution();
            dce.getResolution()
            print("res: "+res)
            ret["resolution"] = res
            call.resolve(ret)
        }
    }
    
    @objc func triggerOnPlayed() {
        if (dce != nil) {
            var ret = PluginCallResultData()
            let res = dce.getResolution()
            ret["resolution"] = res
            print("trigger on played")
            notifyListeners("onPlayed", data: ret)
        }
    }
    
    @objc func getAllCameras(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            var ret = PluginCallResultData()
            let array = NSMutableArray();
            array.addObjects(from: dce.getAllCameras())
            ret["cameras"] = array
            call.resolve(ret)
        }
    }
    
    @objc func getSelectedCamera(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            var ret = PluginCallResultData()
            ret["selectedCamera"] = dce.getSelectedCamera()
            call.resolve(ret)
        }
    }
    
    @objc func selectCamera(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            let cameraID = call.getString("cameraID") ?? ""
            if cameraID != "" {
                try? dce.selectCamera(cameraID)
                triggerOnPlayed()
            }
            call.resolve()
        }
    }
    
    @objc func setScanRegion(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            let region = call.getObject("region")
            let scanRegion = iRegionDefinition()
            scanRegion.regionTop = region?["top"] as! Int
            scanRegion.regionBottom = region?["bottom"] as! Int
            scanRegion.regionLeft = region?["left"] as! Int
            scanRegion.regionRight = region?["right"] as! Int
            scanRegion.regionMeasuredByPercentage = region?["measuredByPercentage"] as! Int
            try? dce.setScanRegion(scanRegion)
            call.resolve()
        }
    }
    
    @objc func setZoom(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            let factor:CGFloat = CGFloat(call.getFloat("factor") ?? 1.0)
            dce.setZoom(factor)
            call.resolve()
        }
    }
    
    @objc func setFocus(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            let x = call.getFloat("x", -1.0);
            let y = call.getFloat("y", -1.0);
            if x != -1.0 && y != -1.0 {
                dce.setFocus(CGPoint(x: CGFloat(x), y: CGFloat(y)))
            }
            call.resolve()
        }
    }
    
    @objc func requestCameraPermission(_ call: CAPPluginCall) {
        call.resolve()
    }
    
    @objc func isOpen(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            var ret = PluginCallResultData()
            if dce.getCameraState() == EnumCameraState.EnumCAMERA_STATE_OPENED {
                ret["isOpen"] = true
            } else {
                ret["isOpen"] = false
            }
            call.resolve(ret)
        }
    }
    
    @objc func takeSnapshot(_ call: CAPPluginCall) {
        if (dce == nil){
            call.reject("DCE not initialized")
        }else{
            let quality = call.getInt("quality",85)
            let frame = dce.getFrameFromBuffer(true)
            
            var ret = PluginCallResultData()
            if let img = frame.toUIImage() {
                let cropped = croppedUIImage(image: img, region: dce.getScanRegion(),degree: frame.orientation)
                let rotated = rotatedUIImage(image: cropped, degree: frame.orientation)
                let base64 = getBase64FromImage(image: rotated, quality: CGFloat(quality/100))
                ret["base64"] = base64
                call.resolve(ret)
            }else{
                call.reject("Failed to take a snapshot")
            }
        }
    }
    
    func rotatedUIImage(image:UIImage, degree: Int) -> UIImage {
        var rotatedImage = UIImage()
        switch degree
        {
            case 90:
                rotatedImage = UIImage(cgImage: image.cgImage!, scale: 1.0, orientation: .right)
            case 180:
                rotatedImage = UIImage(cgImage: image.cgImage!, scale: 1.0, orientation: .down)
            default:
                return image
        }
        return rotatedImage
    }
    
    func croppedUIImage(image:UIImage, region:iRegionDefinition, degree: Int) -> UIImage {
        let cgImage = image.cgImage
        let imgWidth = Double(cgImage!.width)
        let imgHeight = Double(cgImage!.height)
        
        var regionLeft = Double(region.regionLeft) / 100.0
        var regionTop = Double(region.regionTop) / 100.0
        var regionWidth = Double(region.regionRight - region.regionLeft) / 100.0
        var regionHeight = Double(region.regionBottom - region.regionTop) / 100.0

        if degree == 90 {
            let temp1 = regionLeft
            regionLeft = regionTop
            regionTop = temp1
            let temp2 = regionWidth
            regionWidth = regionHeight
            regionHeight = temp2
            print("degree 90")
        }else if degree == 180 {
            regionTop = 1.0 - regionTop
            print("degree 180")
        }
        let left:Double = regionLeft * imgWidth
        let top:Double = regionTop * imgHeight
        let width:Double = regionWidth * imgWidth
        let height:Double = regionHeight * imgHeight
        //print("imgWidth")
        //print(imgWidth)
        //print("regionTop")
        //print(regionTop)
        //print("top")
        //print(top)
        //print("regionleft")
        //print(regionLeft)
        //print("left")
        //print(left)
        //print("regionWidth")
        //print(regionWidth)
        //print("regionHeight")
        //print(regionHeight)
        //print("width")
        //print(width)
        //print("height")
        //print(height)
        
        // The cropRect is the rect of the image to keep,
        // in this case centered
        let cropRect = CGRect(
            x: left,
            y: top,
            width: width,
            height: height
        ).integral

        let cropped = cgImage?.cropping(
            to: cropRect
        )!
        let image = UIImage(cgImage: cropped!)
        return image
    }
    
    func getBase64FromImage(image:UIImage, quality: CGFloat) -> String{
       let dataTmp = image.jpegData(compressionQuality: quality)
       if let data = dataTmp {
           return data.base64EncodedString()
       }
       return ""
    }
    
    @objc func takePhoto(_ call: CAPPluginCall) {
        takeSnapshot(call)
    }
    
}
