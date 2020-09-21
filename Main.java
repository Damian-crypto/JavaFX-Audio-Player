import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

import javafx.geometry.Insets;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.io.File;

import java.net.MalformedURLException;

public class Main extends Application {

	private StackPane root;
	private VBox rootTree;
	private File audioFile;
	private Media media;
	private MediaPlayer mediaPlayer;
	private MediaPlayer.Status currentStatus = MediaPlayer.Status.PAUSED;
	private Slider timeSlider;
	private Button playBtn;
	private ImageView playImg, pauseImg;

	private boolean initializer(Stage stage) {
		HBox buttonsPanel = new HBox(10);

		Button openBtn = new Button();
		openBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/open.png"))));
		openBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evt) {
				createMediaPlayer(stage);
			}
		});

		playBtn = new Button();
		playBtn.setGraphic(playImg);
		playBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evt) {
				if(mediaPlayer != null) {
					if(currentStatus == MediaPlayer.Status.PLAYING) {
						currentStatus = MediaPlayer.Status.PAUSED;
						playBtnUpdate();
						mediaPlayer.pause();
					} else if(currentStatus == MediaPlayer.Status.PAUSED || currentStatus == MediaPlayer.Status.STOPPED || mediaPlayer.getStatus() == MediaPlayer.Status.READY) {
						currentStatus = MediaPlayer.Status.PLAYING;
						startTimeSlider();
						playBtnUpdate();
						mediaPlayer.play();
					}
				}
			}
		});

		Button restoreBtn = new Button();
		restoreBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/repeat.png"))));
		restoreBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evt) {
				if(mediaPlayer != null) {
					playBtnUpdate();
					mediaPlayer.seek(mediaPlayer.getStartTime());
				}
			}
		});

		Button stopBtn = new Button();
		stopBtn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/stop.png"))));
		stopBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evt) {
				if(mediaPlayer != null) {
					if(currentStatus == MediaPlayer.Status.PLAYING) {
						currentStatus = MediaPlayer.Status.STOPPED;
						playBtnUpdate();
						mediaPlayer.stop();
					}
				}
			}
		});

		timeSlider = new Slider();

		timeSlider.valueProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable obs) {
				if(mediaPlayer != null) {
					if(timeSlider.isPressed()) {
						mediaPlayer.seek(mediaPlayer.getMedia().getDuration().multiply(timeSlider.getValue() / 100));
					}
				}
			}
		});

		Label volImg = new Label();
		volImg.setPadding(new Insets(3, 0, 0, 0));
		volImg.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/icons/vol.png"))));
		Slider volSlider = new Slider(0, 100, 100);
		volSlider.setPadding(new Insets(10, 0, 0, 0));
		volSlider.setPrefWidth(100.0);
		volSlider.valueProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable obs) {
				if(mediaPlayer != null) {
					if(volSlider.isPressed()) {
						mediaPlayer.setVolume(volSlider.getValue() / 100);
					}
				}
			}
		});

		buttonsPanel.getChildren().addAll(openBtn, playBtn, restoreBtn, stopBtn, volImg, volSlider);
		rootTree.getChildren().addAll(buttonsPanel, timeSlider);
		return true;
	}

	private void createMediaPlayer(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Audio files", "*.mp3", "*.wav"));
		audioFile = fileChooser.showOpenDialog(stage);
		if(audioFile != null) {
			try {
				media = new Media(audioFile.toURI().toURL().toExternalForm());
				if(mediaPlayer == null) {
					mediaPlayer = new MediaPlayer(media);
				} else {
					mediaPlayer.stop();
					mediaPlayer = null;
					mediaPlayer = new MediaPlayer(media);
				}
				if(!mediaPlayer.getMedia().getSource().equals("")) {
					currentStatus = MediaPlayer.Status.PLAYING;
					playBtnUpdate();
					mediaPlayer.play();
					startTimeSlider();
				} else {
					currentStatus = MediaPlayer.Status.PAUSED;
				}
			} catch(MalformedURLException e) {
				System.err.println("File error:");
				e.printStackTrace();
			}
		} else {
			Alert alert = new Alert(Alert.AlertType.WARNING, "File selection failed!", ButtonType.OK);
			alert.show();
		}
	}

	private void playBtnUpdate() {
		if(currentStatus == MediaPlayer.Status.PLAYING) {
			playBtn.setGraphic(pauseImg);
		} else {
			playBtn.setGraphic(playImg);
		}
	}

	private void startTimeSlider() {
		if(mediaPlayer != null) {
			mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
				@Override
				public void invalidated(Observable obs) {
					updateValues();
				}
			});
		}
	}

	private void updateValues() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				timeSlider.setValue((mediaPlayer.getCurrentTime().toMillis() / mediaPlayer.getTotalDuration().toMillis()) * 100);
			}
		});
	}

	@Override
	public void start(final Stage primaryStage) {
		root = new StackPane();
		root.setPadding(new Insets(10, 10, 10, 10));
		rootTree = new VBox(10);
		playImg = new ImageView(new Image(getClass().getResourceAsStream("/icons/play.png")));
		pauseImg = new ImageView(new Image(getClass().getResourceAsStream("/icons/pause.png")));

		boolean ok = initializer(primaryStage);

		root.getChildren().add(rootTree);

		primaryStage.setScene(new Scene(root, 420, 100));
		primaryStage.setTitle("Simple mp3 player");
		if(ok) primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
