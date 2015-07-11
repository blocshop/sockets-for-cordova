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
using System.Text;
using System.Threading.Tasks;

namespace Blocshop.ScoketsForCordova
{
    public interface ISocketStorage
    {
        void Add(string socketKey, ISocketAdapter socketAdapter);
        ISocketAdapter Get(string socketKey);
        void Remove(string socketKey);
    }

    public class SocketStorage : ISocketStorage
    {
        private readonly IDictionary<string, ISocketAdapter> socketAdapters = new Dictionary<string, ISocketAdapter>();

        private object syncRoot = new object();

        public void Add(string socketKey, ISocketAdapter socketAdapter)
        {
            lock (syncRoot)
            {
                System.Diagnostics.Debug.WriteLine("Add: " + DateTime.Now.Ticks);
                this.socketAdapters.Add(socketKey, socketAdapter);
            }
        }

        public ISocketAdapter Get(string socketKey)
        {
            lock (syncRoot)
            {
                System.Diagnostics.Debug.WriteLine("Get: " + DateTime.Now.Ticks);
                if (!this.socketAdapters.ContainsKey(socketKey))
                {
                    throw new ArgumentException(
                        string.Format("Cannot find socketKey: {0}. Connection is probably closed.", socketKey));
                }

                return this.socketAdapters[socketKey];
            }
        }

        public void Remove(string socketKey)
        {
            lock (syncRoot)
            {
                System.Diagnostics.Debug.WriteLine("Remove: " + DateTime.Now.Ticks);
                this.socketAdapters.Remove(socketKey);
            }
        }

        public static ISocketStorage CreateSocketStorage()
        {
            return new SocketStorage();
        }
    }
}
