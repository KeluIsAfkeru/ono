package moe.ono.service

import com.tencent.common.app.AppInterface
import com.tencent.qphone.base.remote.ToServiceMsg
import com.tencent.qphone.base.remote.FromServiceMsg

import com.tencent.qphone.base.remote.IBaseActionListener
import mqq.app.MobileQQ

abstract class QQInterfaces {

    companion object {
        val app = (if (PlatformUtils.isMqqPackage())
            MobileQQ.getMobileQQ().waitAppRuntime()
        else
            MobileQQ.getMobileQQ().waitAppRuntime(null)) as AppInterface

        private fun sendToServiceMsg(to: ToServiceMsg) {
            app.sendToService(to)
        }

        private fun createToServiceMsg(cmd: String): ToServiceMsg {
            return ToServiceMsg("mobileqq.service", app.currentAccountUin, cmd)
        }

        fun sendBuffer(
            cmd: String,
            isProto: Boolean,
            data: ByteArray,
        ) {
            val toServiceMsg = createToServiceMsg(cmd)
            toServiceMsg.putWupBuffer(data)
            toServiceMsg.addAttribute("req_pb_protocol_flag", isProto)
            sendToServiceMsg(toServiceMsg)
        }

        //新加一个可以回调的发包函数
        fun sendBufferWithResponse(
    cmd: String,
    isProto: Boolean,
    data: ByteArray,
    callback: (response: FromServiceMsg) -> Unit  // 修改回调参数类型为FromServiceMsg
) {
    val toServiceMsg = createToServiceMsg(cmd)
    toServiceMsg.putWupBuffer(data)
    toServiceMsg.addAttribute("req_pb_protocol_flag", isProto)

    //使用反射修改needResp为true
    try {
        val field = ToServiceMsg::class.java.getDeclaredField("needResp")
        field.isAccessible = true
        field.setBoolean(toServiceMsg, true)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // 设置 actionListener
    toServiceMsg.actionListener = object : IBaseActionListener.Stub() {
        override fun onActionResult(response: FromServiceMsg) {
            callback(response) 
        }
        
        override fun onRecvFromMsg(response: FromServiceMsg) {
            
        }
    }

    sendToServiceMsg(toServiceMsg)
        }
    }
}
