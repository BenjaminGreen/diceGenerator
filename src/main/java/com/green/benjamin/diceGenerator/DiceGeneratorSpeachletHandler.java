package com.green.benjamin.diceGenerator;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

public class DiceGeneratorSpeachletHandler extends SpeechletRequestStreamHandler {

  private static final Set<String> supportedApplicationIds;

  static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
    supportedApplicationIds = new HashSet<String>();
    supportedApplicationIds.add("amzn1.ask.skill.9d4ddeaf-bfbe-40a2-8774-ecdec7de3f64");
  }

  public DiceGeneratorSpeachletHandler() {
    super(new DiceGeneratorSpeechlet(), supportedApplicationIds);
  }

  public DiceGeneratorSpeachletHandler(Speechlet speechlet,
                                       Set<String> supportedApplicationIds) {
    super(speechlet, supportedApplicationIds);
  }

}