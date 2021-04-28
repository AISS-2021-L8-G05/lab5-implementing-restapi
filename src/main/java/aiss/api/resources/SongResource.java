package aiss.api.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

import aiss.api.resources.comparators.ComparatorAlbumSong;
import aiss.api.resources.comparators.ComparatorAlbumSongReversed;
import aiss.api.resources.comparators.ComparatorArtistSong;
import aiss.api.resources.comparators.ComparatorArtistSongReversed;
import aiss.api.resources.comparators.ComparatorYearSong;
import aiss.api.resources.comparators.ComparatorYearSongReversed;
import aiss.model.Song;
import aiss.model.repository.MapPlaylistRepository;
import aiss.model.repository.PlaylistRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;



@Path("/songs")
public class SongResource {

	public static SongResource _instance=null;
	PlaylistRepository repository;
	
	private SongResource(){
		repository=MapPlaylistRepository.getInstance();
	}
	
	public static SongResource getInstance()
	{
		if(_instance==null)
			_instance=new SongResource();
		return _instance; 
	}
	
	@GET
	@Produces("application/json")
	public Collection<Song> getAll(@QueryParam("q") String q, @QueryParam("order") String order, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit)
	{
		List<Song> result = new ArrayList<Song>();
		
		for(Song song : repository.getAllSongs()) {
			if(q == null || song.getTitle().contains(q) || song.getAlbum().contains(q) || song.getArtist().contains(q)) {
				result.add(song);
			}
		}
		
		if(order != null) {
			if(order.equals("album")) {
				Collections.sort(result, new ComparatorAlbumSong());
			}else if(order.equals("-album")) {
				Collections.sort(result, new ComparatorAlbumSongReversed());
			}else if(order.equals("artist")) {
				Collections.sort(result, new ComparatorArtistSong());
			}else if(order.equals("-artist")){
				Collections.sort(result, new ComparatorArtistSongReversed());
			}else if(order.equals("year")) {
				Collections.sort(result, new ComparatorYearSong());
			}else if(order.equals("-year")) {
				Collections.sort(result, new ComparatorYearSongReversed());
			}else {
				throw new BadRequestException("The parameter order isn't correct");
			}
		}
		
		if (offset != null && offset<0) throw new BadRequestException("Offset must be >=0 but was "+offset+"!");
	    if (limit != null && limit<-1) throw new BadRequestException("Limit must be >=-1 but was "+limit+"!");

	    if (offset != null && offset>0) {
	        if (offset >= result.size()) {
	            result = result.subList(0, 0); //return empty.
	        }
	        if (limit != null && limit >-1) {
	            //apply offset and limit
	            result = result.subList(offset, Math.min(offset+limit, result.size()));
	        } else {
	            //apply just offset
	            result = result.subList(offset, result.size());
	        }
	    } else if (limit != null && limit >-1) {
	        //apply just limit
	        result = result.subList(0, Math.min(limit, result.size()));
	    } else {
	      result = result.subList(0, result.size());
	    }
		
		return result;
	}
	
	
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Song get(@PathParam("id") String songId)
	{
		Song song = repository.getSong(songId);
		
		if (song == null) {
			throw new BadRequestException("The song with id="+songId+" was not found");
		}
		
		return song;
	}
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response addSong(@Context UriInfo uriInfo, Song song) {
		
		if(song.getTitle()==null || "".equals(song.getTitle())) {
			throw new BadRequestException("The title od the song must not be null");
		}
		
		repository.addSong(song);
		
		// BUilds the response. Returns the song the has just been added.
		UriBuilder ub = uriInfo.getAbsolutePathBuilder().path(this.getClass(),"get");
		URI uri = ub.build(song.getId());
		ResponseBuilder resp = Response.created(uri);
		resp.entity(song);
		
		return resp.build();
	}
	
	
	@PUT
	@Consumes("application/json")
	public Response updateSong(Song song) {
		
		Song oldSong = repository.getSong(song.getId());
		
		if(oldSong == null) {
			throw new NotFoundException("The song with id="+song.getId()+" was not found");
		}
		
		// Update title
		if(song.getTitle() != null) {
			oldSong.setTitle(song.getTitle());
		}
		
		// Update artist
		if(song.getArtist() != null) {
			oldSong.setArtist(song.getArtist());
		}
		
		// Update album
		if(song.getAlbum() != null) {
			oldSong.setAlbum(song.getAlbum());
		}
		
		// Update year
		if(song.getYear() != null) {
			oldSong.setYear(song.getYear());
		}
		
		return Response.noContent().build();
	}
	
	@DELETE
	@Path("/{id}")
	public Response removeSong(@PathParam("id") String songId) {
		Song toberemoved = repository.getSong(songId);
		
		if(toberemoved == null) {
			throw new NotFoundException("The song with id="+songId+" was not found");
		}else {
			repository.deleteSong(songId);
		}
		return Response.noContent().build();
	}
	
}
