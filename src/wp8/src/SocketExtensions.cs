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
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace Blocshop.ScoketsForCordova
{
    public static class SocketExtensions
    {
        public static async Task ConnectTaskAsync(this Socket socket, SocketAsyncEventArgs socketAsyncEventArgs)
        {
            var task = CreateTaskFromCompletionHandler(socketAsyncEventArgs, SocketAsyncOperation.Connect);

            socket.ConnectAsync(socketAsyncEventArgs);

            await task;
        }

        public static async Task SendTaskAsync(this Socket socket, SocketAsyncEventArgs socketAsyncEventArgs)
        {
            var task = CreateTaskFromCompletionHandler(socketAsyncEventArgs, SocketAsyncOperation.Send);
            
            socket.SendAsync(socketAsyncEventArgs);

            await task;
        }

        public static async Task<byte[]> ReceiveTaskAsync(this Socket socket, SocketAsyncEventArgs socketAsyncEventArgs)
        {
            var task = CreateTaskFromCompletionHandler(socketAsyncEventArgs, SocketAsyncOperation.Receive);

            socket.ReceiveAsync(socketAsyncEventArgs);

            await task;

            return socketAsyncEventArgs.Buffer;
        }

        private static Task CreateTaskFromCompletionHandler(SocketAsyncEventArgs socketAsyncEventArgs, SocketAsyncOperation socketAsyncOperation)
        {
            TaskCompletionSource<string> completionSource = new TaskCompletionSource<string>();
            socketAsyncEventArgs.Completed += new EventHandler<SocketAsyncEventArgs>((o, eventArgs) =>
            {
                if (eventArgs.LastOperation == socketAsyncOperation)
                {
                    if (eventArgs.SocketError == SocketError.Success)
                    {
                        completionSource.SetResult("");
                    }
                    else
                    {
                        completionSource.SetException(new SocketException((int)eventArgs.SocketError));
                    }
                }
            });

            return completionSource.Task;
        }
    }
}
