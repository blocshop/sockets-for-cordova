/**
 * Copyright (c) 2015, Blocshop s.r.o.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the Blocshop s.r.o.. The name of the
 * Blocshop s.r.o. may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

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
