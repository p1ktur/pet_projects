package my.app.hanoitower

class Rod(pos: Float, number: Int, discs: List<Disc>) {
    var position = pos
    var discs = discs
    var number = number

    fun replaceDiscs(loser: Rod){
        val winner = this
        if (loser.discs.isNotEmpty()){
            winner.discs = winner.discs.plus(Disc(loser.discs[loser.discs.size-1].level, winner.number, 0f to 0f, loser.discs[loser.discs.size-1].paint))
            loser.discs = loser.discs.dropLast(1)
        }
    }

}