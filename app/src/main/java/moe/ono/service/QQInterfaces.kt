package moe.ono.service

import com.tencent.common.app.AppInterface
import com.tencent.qphone.base.remote.ToServiceMsg
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
    callback: (response: Any?) -> Unit // 添加一个回调函数
) {
    val toServiceMsg = createToServiceMsg(cmd)
    toServiceMsg.putWupBuffer(data)
    toServiceMsg.addAttribute("req_pb_protocol_flag", isProto)
    toServiceMsg.needResp = true //标记哈是否需要响应

    //响应回调
    toServiceMsg.actionListener = object : IBaseActionListener.Stub() {
        override fun onActionResult(response: Any?) {
            callback(response) //回调返回结果
        }
    }

    sendToServiceMsg(toServiceMsg)
        }
    }
}
