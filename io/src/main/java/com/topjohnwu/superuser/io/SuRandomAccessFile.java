/*
 * Copyright 2020 John "topjohnwu" Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.topjohnwu.superuser.io;

import com.topjohnwu.superuser.internal.IOFactory;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Access files using the global shell instance and mimics {@link RandomAccessFile}.
 * <p>
 * This class always checks whether using a shell is necessary. If not, it simply opens a new
 * {@link RandomAccessFile} and behaves as a wrapper. This class has the exact same
 * methods as {@link RandomAccessFile} and can be treated as a drop-in replacement.
 * <p>
 * File random access via shell is extremely limited. Each I/O operation comes with a relatively
 * large overhead. For optimal performance, please consider using {@link SuFileInputStream} and
 * {@link SuFileOutputStream} since those classes are specifically optimized for I/O throughput.
 * @see RandomAccessFile
 */
public abstract class SuRandomAccessFile implements DataInputPlus, DataOutput, Closeable {

    /**
     * @param file the file object.
     * @param mode the access mode.
     *             Note: {@code rws}, {@code rwd} behaves exactly the same as {@code rw} if
     *             it end up using shell-backed implementation.
     * @return an instance of {@link SuRandomAccessFile}.
     * @throws FileNotFoundException
     * @see RandomAccessFile#RandomAccessFile(File, String)
     */
    public static SuRandomAccessFile open(File file, String mode) throws FileNotFoundException {
        if (file instanceof SuFile) {
            return IOFactory.createShellIO((SuFile) file, mode);
        } else {
            try {
                return IOFactory.createRAFWrapper(file, mode);
            } catch (FileNotFoundException e) {
                return IOFactory.createShellIO(new SuFile(file), mode);
            }
        }
    }

    /**
     * {@code SuRandomAccessFile.open(new File(path), mode)}
     */
    public static SuRandomAccessFile open(String path, String mode) throws FileNotFoundException {
        return open(new File(path), mode);
    }

    /**
     * @see RandomAccessFile#seek(long)
     */
    public abstract void seek(long pos) throws IOException;

    /**
     * @see RandomAccessFile#seek(long)
     */
    public abstract void setLength (long newLength) throws IOException;

    /**
     * @see RandomAccessFile#length()
     */
    public abstract long length() throws IOException;

    /**
     * @see RandomAccessFile#getFilePointer()
     */
    public abstract long getFilePointer() throws IOException;

    /**
     * Will throw {@link UnsupportedOperationException} if this instance is shell-backed.
     * @see RandomAccessFile#getFD()
     */
    public abstract FileDescriptor getFD() throws IOException;

    /**
     * Will throw {@link UnsupportedOperationException} if this instance is shell-backed.
     * @see RandomAccessFile#getChannel()
     */
    public abstract FileChannel getChannel();
}
