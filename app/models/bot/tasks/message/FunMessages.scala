package models.bot.tasks.message

/**
 * A list of fun, pre-defined message trees.
 */
object FunMessages {

  def messages = List(
    MessageTree(
      value = "{name} are you a fan of traveling?",
      right = Some(MessageTree(
        value = "So if I asked you to travel with me you'd do it?",
        right = Some(MessageTree(
						value = "Well, you should download Wander, my friend's trying it out and she's loving it! We should chat on there!",
						right = None,
						left = Some(MessageTree(
								value = "Aw, but I really liked you...and I don't wanna chat on Tinder, I'd rather chat on Wander :) ",
								right = Some(MessageTree(
									value = "Yay! Go go go and download it and I'll give you my contact info",
									right = None,
									left = None
									)),
								left = Some(MessageTree(
										value = "Well, alright, bye!",
										right = None,
										left = None
									))
							))
					)),
        left = Some(MessageTree(
						value = "Oh, that's a shame, never mind",
						right = None,
						left = None
					))
      )),
      left = Some(MessageTree(
        value = "{name}, do you love anything more than traveling?",
				right = Some(MessageTree(
					value = "So if I asked you to travel with me you'd do it?",
					right = Some(MessageTree(
						value = "Well, you should download Wander, my friend's trying it out and she's loving it! We should chat on there!",
						right = None,
						left = Some(MessageTree(
							value = "Aw, but I really liked you...and I don't wanna chat on Tinder, I'd rather chat on Wander :) ",
							right = Some(MessageTree(
								value = "Yay! Go go go and download it and I'll give you my contact info",
								right = None,
								left = None
							)),
							left = Some(MessageTree(
									value = "Well, alright, bye!",
									right = None,
									left = None
							))
						))
				)),       
        left = None
      ))
    )))
	)

}
