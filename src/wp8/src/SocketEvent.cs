using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;
using System.Threading.Tasks;

namespace Blocshop.ScoketsForCordova
{
    [DataContract]
    public abstract class SocketEvent
    {
        [DataMember(Name = "type")]
        public abstract string Type { get; set; }

        [DataMember(Name = "socketKey")]
        public string SocketKey { get; set; }
    }

    [DataContract]
    public class CloseSocketEvent : SocketEvent
    {
        public override string Type 
        { 
            get 
            { 
                return "Close"; 
            }
            set
            {
            }
        }

        [DataMember(Name = "hasError")]
        public bool HasError { get; set; }
    }

    [DataContract]
    public class DataReceivedSocketEvent : SocketEvent
    {
        public override string Type 
        { 
            get 
            { 
                return "DataReceived"; 
            }
            set
            {
            }
        }

        [DataMember(Name = "data")]
        public byte[] Data { get; set; }
    }

    [DataContract]
    public class ErrorSocketEvent : SocketEvent
    {
        public override string Type 
        { 
            get 
            { 
                return "Error"; 
            }
            set
            {
            }
        }

        [DataMember(Name="errorMessage")]
        public string ErrorMessage { get; set; }
    }
}
