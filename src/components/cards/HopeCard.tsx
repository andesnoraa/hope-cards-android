import { useState } from "react";

import type { Verse } from "../../types/verse";
import CardBack from "./CardBack";
import CardFront from "./CardFront";

type Props = {
  verse: Verse;
};

export default function HopeCard({ verse }: Props) {
  const [showFront] = useState(true);

  if (showFront) {
    return <CardFront verse={verse} />;
  }

  return <CardBack />;
}