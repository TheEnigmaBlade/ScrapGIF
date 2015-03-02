package net.enigmablade.gif.img.webm;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import com.alee.log.*;
import com.xuggle.mediatool.*;
import com.xuggle.mediatool.event.*;
import com.xuggle.xuggler.*;
import net.enigmablade.gif.img.*;

public class WebMLoader extends ImageLoader implements IMediaListener
{
	public static final double SECONDS_BETWEEN_FRAMES = 0.042;
	public static final long MICRO_SECONDS_BETWEEN_FRAMES =	(long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
	
	private int usedVideoStreamIndex = -1;
	private long mLastPtsWrite = Global.NO_PTS;
	
	private List<ImageFrame> frameBuffer;
	private boolean frameRead = false;
	
	public WebMLoader()
	{
		super("webm");
	}
	
	@Override
	protected ImageFrame[] loadImage(File file, boolean onlyFirstFrame)
	{
		frameBuffer = new LinkedList<>();
		
		IMediaReader reader = ToolFactory.makeReader(file.getAbsolutePath());
		reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		reader.addListener(this);
		
		IError error;
		// Read frames until an error or optionally until a single frame has been read
		while((error = reader.readPacket()) == null && (!onlyFirstFrame || (onlyFirstFrame && !frameRead)));
		if(error != null && error.getType() != IError.Type.ERROR_EOF)
			Log.error("Decode failure: "+error.getType()+"; "+error.getDescription());
		
		return frameBuffer.toArray(new ImageFrame[frameBuffer.size()]);
	}
	
	@Override
	public void onVideoPicture(IVideoPictureEvent evt)
	{
		if(evt.getStreamIndex() != usedVideoStreamIndex)
		{
			// Mark the stream as the one being used
			if(usedVideoStreamIndex == -1)
				usedVideoStreamIndex = evt.getStreamIndex();
			// Ignore all other streams
			else
				return;
		}
		
		// Initialize (if uninitialized) to the first frame
		if(mLastPtsWrite == Global.NO_PTS)
			mLastPtsWrite = evt.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;
		
		// Process if the proper amount of time has passed
		if(evt.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES)
		{
			frameBuffer.add(new ImageFrame(evt.getImage(), (int)TimeUnit.MICROSECONDS.toMillis(MICRO_SECONDS_BETWEEN_FRAMES), null));
		}
	}
	
	// Unused
	
	@Override
	public void onAddStream(IAddStreamEvent evt) {}
	@Override
	public void onAudioSamples(IAudioSamplesEvent evt) {}
	@Override
	public void onClose(ICloseEvent evt) {}
	@Override
	public void onCloseCoder(ICloseCoderEvent evt) {}
	@Override
	public void onFlush(IFlushEvent evt) {}
	@Override
	public void onOpen(IOpenEvent evt) {}
	@Override
	public void onOpenCoder(IOpenCoderEvent evt) {}
	@Override
	public void onReadPacket(IReadPacketEvent evt) {}
	@Override
	public void onWriteHeader(IWriteHeaderEvent evt) {}
	@Override
	public void onWritePacket(IWritePacketEvent evt) {}
	@Override
	public void onWriteTrailer(IWriteTrailerEvent evt) {}
}
