package ru.mipt.edf;

import java.util.ArrayList;
import java.util.List;

public class Annotation
{
	private double onSet = 0;
	private double duration = 0;
	private List<String> annotations = new ArrayList<String>();

	public static List<Annotation> parseAnnotations(byte[] b)
	{
		List<Annotation> annotations = new ArrayList<Annotation>();
		int onSetIndex = 0;
		int durationIndex = -1;
		int annotationIndex = -2;
		int endIndex = -3;
		for (int i = 0; i < b.length - 1; i++)
		{
			if (b[i] == 21)
			{
				durationIndex = i;
				continue;
			}
			if (b[i] == 20 && onSetIndex > annotationIndex)
			{
				annotationIndex = i;
				continue;
			}
			if (b[i] == 20 && b[i + 1] == 0)
			{
				endIndex = i;
				continue;
			}
			if (b[i] != 0 && onSetIndex < endIndex)
			{

				String onSet = null;
				String duration = null;
				if (durationIndex > onSetIndex)
				{
					onSet = new String(b, onSetIndex, durationIndex - onSetIndex);
					duration = new String(b, durationIndex, annotationIndex - durationIndex);
				} else
				{
					onSet = new String(b, onSetIndex, annotationIndex - onSetIndex);
					duration = "";
				}
				String annotation = new String(b, annotationIndex, endIndex - annotationIndex);
				annotations.add(new Annotation(onSet, duration, annotation.split("[\u0014]")));
				onSetIndex = i;
			}
		}
		return annotations;
	}

	private Annotation(String onSet, String duration, String[] annotations)
	{
		this.onSet = Double.parseDouble(onSet);
		if (duration != null && duration != "")
			this.duration = Double.parseDouble(duration);
		for (int i = 0; i < annotations.length; i++)
		{
			if (annotations[i] == null || annotations[i].trim().equals(""))
				continue;
			this.annotations.add(annotations[i]);
		}
	}

	public double getOnSet()
	{
		return onSet;
	}

	public double getDuration()
	{
		return duration;
	}

	public List<String> getAnnotations()
	{
		return annotations;
	}

	@Override
	public String toString()
	{
		return "Annotation [onSet=" + onSet + ", duration=" + duration + ", annotations=" + annotations + "]";
	}
}
