package fr.raksrinana.twitchminer.miner.streamer;

import fr.raksrinana.twitchminer.api.gql.data.channelpointscontext.ChannelPointsContextData;
import fr.raksrinana.twitchminer.api.gql.data.dropshighlightserviceavailabledrops.DropsHighlightServiceAvailableDropsData;
import fr.raksrinana.twitchminer.api.gql.data.types.*;
import fr.raksrinana.twitchminer.api.gql.data.videoplayerstreaminfooverlaychannel.VideoPlayerStreamInfoOverlayChannelData;
import fr.raksrinana.twitchminer.factory.TimeFactory;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Log4j2
public class Streamer{
	@NotNull
	@Getter
	@EqualsAndHashCode.Include
	private final String id;
	@NotNull
	@Getter
	@ToString.Include
	private final String username;
	@NotNull
	@Getter
	private StreamerSettings settings;
	@Setter
	private Instant lastUpdated = Instant.EPOCH;
	
	private URL channelUrl;
	
	@Nullable
	@Setter
	private ChannelPointsContextData channelPointsContext;
	@Nullable
	@Setter
	private VideoPlayerStreamInfoOverlayChannelData videoPlayerStreamInfoOverlayChannel;
	@Nullable
	@Setter
	private DropsHighlightServiceAvailableDropsData dropsHighlightServiceAvailableDrops;
	@Nullable
	@Setter
	@Getter
	private URL spadeUrl;
	
	public boolean updateCampaigns(){
		return true;
	}
	
	public boolean followRaids(){
		return settings.isFollowRaid();
	}
	
	public boolean needUpdate(){
		return TimeFactory.now().isAfter(lastUpdated.plus(5, MINUTES));
	}
	
	public int getScore(){
		return settings.getPriorities().stream()
				.mapToInt(p -> p.getScore(this))
				.sum();
	}
	
	public Collection<CommunityPointsMultiplier> getActiveMultipliers(){
		return ofNullable(channelPointsContext)
				.map(ChannelPointsContextData::getCommunity)
				.map(User::getChannel)
				.map(Channel::getSelf)
				.map(ChannelSelfEdge::getCommunityPoints)
				.map(CommunityPointsProperties::getActiveMultipliers)
				.orElse(List.of());
	}
	
	@NotNull
	public Optional<Integer> getChannelPoints(){
		return ofNullable(channelPointsContext)
				.map(ChannelPointsContextData::getCommunity)
				.map(User::getChannel)
				.map(Channel::getSelf)
				.map(ChannelSelfEdge::getCommunityPoints)
				.map(CommunityPointsProperties::getBalance);
	}
	
	@Nullable
	public URL getChannelUrl(){
		if(Objects.isNull(channelUrl)){
			try{
				channelUrl = URI.create("https://www.twitch.tv/").resolve(getUsername()).toURL();
			}
			catch(MalformedURLException e){
				log.error("Failed to construct streamer url", e);
			}
		}
		return channelUrl;
	}
	
	public Optional<String> getClaimId(){
		return ofNullable(channelPointsContext)
				.map(ChannelPointsContextData::getCommunity)
				.map(User::getChannel)
				.map(Channel::getSelf)
				.map(ChannelSelfEdge::getCommunityPoints)
				.map(CommunityPointsProperties::getAvailableClaim)
				.map(CommunityPointsClaim::getId);
	}
	
	@NotNull
	public Optional<Game> getGame(){
		return ofNullable(videoPlayerStreamInfoOverlayChannel)
				.map(VideoPlayerStreamInfoOverlayChannelData::getUser)
				.map(User::getBroadcastSettings)
				.map(BroadcastSettings::getGame);
	}
	
	@NotNull
	public Optional<String> getStreamId(){
		return getStream().map(Stream::getId);
	}
	
	@NotNull
	private Optional<Stream> getStream(){
		return ofNullable(videoPlayerStreamInfoOverlayChannel)
				.map(VideoPlayerStreamInfoOverlayChannelData::getUser)
				.map(User::getStream);
	}
	
	public boolean isStreamingGame(){
		return getGame()
				.map(Game::getName)
				.map(game -> !game.isBlank())
				.orElse(false);
	}
	
	public boolean isStreaming(){
		return getStream().isPresent();
	}
}