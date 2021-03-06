package aiss.api.resources.comparators;

import java.util.Comparator;

import aiss.model.Song;

public class ComparatorYearSongReversed implements Comparator<Song>{

	@Override
	public int compare(Song s1, Song s2) {
		return s2.getYear().compareTo(s1.getYear());
	}

}
