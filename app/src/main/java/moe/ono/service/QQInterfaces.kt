package moe.ono.service

import com.tencent.common.app.AppInterface
import com.tencent.qphone.base.remote.ToServiceMsg
import com.tencent.qphone.base.remote.FromServiceMsg
import mqq.app.MobileQQ

abstract class QQInterfaces {

    companion object {
        val app = (if (PlatformUtils.isMqqPackage())
            MobileQQ.getMobileQQ().waitAppRuntime()
        else
            MobileQQ.getMobileQQ().waitAppRuntime(null)) as AppInterface
            
        private val pendingRequests = mutableMapOf<Int, String>()
        

        private fun sendToServiceMsg(to: ToServiceMsg) {
            app.sendToService(to)
        }

        private fun createToServiceMsg(cmd: String): ToServiceMsg {
            return ToServiceMsg("mobileqq.service", app.currentAccountUin, cmd)
        }

        fun sendBuffer (
            cmd: String,
            isProto: Boolean,
            data: ByteArray,
            onResponse: ((FromServiceMsg) -> Unit)? = null //可选回调
        ): Int {
            val toServiceMsg = createToServiceMsg(cmd)
            toServiceMsg.putWupBuffer(data)
            toServiceMsg.addAttribute("req_pb_protocol_flag", isProto)
            sendToServiceMsg(toServiceMsg)
            
            val appSeq = toServiceMsg.getAppSeq()
            pendingRequests[appSeq] = cmd
            onResponse?.let { responseCallbacks[appSeq] = it }
            return appSeq
        }

        /**
         * 检查服务端响应，匹配appSeq
         */
        fun checkResponse(from: FromServiceMsg) {
            /*val toServiceMsg = from.getToServiceMsg() //获取关联的ToServiceMsg
            val appSeq = toServiceMsg?.appSeq ?: return*/
            val appSeq = from.getAppSeq()
            
            // 检查是否是 pendingRequests中的请求
            if (pendingRequests.containsKey(appSeq)) {
                val cmd = pendingRequests[appSeq]
                pendingRequests.remove(appSeq)
                
                //执行回调
                responseCallbacks[appSeq]?.invoke(from)
                responseCallbacks.remove(appSeq)
                
                
            }
            
        }

        // 存储回调
        private val responseCallbacks = mutableMapOf<Int, (FromServiceMsg) -> Unit>()
    }
}
