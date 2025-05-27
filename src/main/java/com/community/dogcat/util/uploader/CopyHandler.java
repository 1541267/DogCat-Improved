package com.community.dogcat.util.uploader;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;

import lombok.Getter;

public class CopyHandler implements CompletionHandler<Integer, Void> {
	private final AsynchronousFileChannel in;
	private final AsynchronousFileChannel out;
	private final ByteBuffer buffer;
	/**
	 * -- GETTER --
	 *  복사가 완료되었을 때 CompletableFuture를 통해 알 수 있습니다.
	 */
	@Getter private final CompletableFuture<Void> future = new CompletableFuture<>();
	private long position = 0L;

	public CopyHandler(AsynchronousFileChannel in,
		AsynchronousFileChannel out,
		ByteBuffer buffer) {
		this.in = in;
		this.out = out;
		this.buffer = buffer;
	}

	/**
	 * 읽기 작업이 완료되면 호출됩니다.
	 * bytesRead < 0 이면 EOF이므로 복사 완료로 간주하고 future를 complete 합니다.
	 */
	@Override
	public void completed(Integer bytesRead, Void attachment) {
		if (bytesRead < 0) {
			future.complete(null);
			return;
		}
		buffer.flip();
		// 읽은 만큼 쓰기
		out.write(buffer, position, null, new CompletionHandler<Integer, Void>() {
			@Override
			public void completed(Integer bytesWritten, Void att) {
				position += bytesWritten;
				buffer.clear();
				// 다음 읽기 요청
				in.read(buffer, position, null, CopyHandler.this);
			}
			@Override
			public void failed(Throwable exc, Void att) {
				future.completeExceptionally(exc);
			}
		});
	}

	/**
	 * 읽기 혹은 쓰기 중 실패 시 호출됩니다.
	 */
	@Override
	public void failed(Throwable exc, Void attachment) {
		future.completeExceptionally(exc);
	}
}
