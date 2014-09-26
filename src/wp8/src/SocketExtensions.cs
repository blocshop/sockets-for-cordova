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
